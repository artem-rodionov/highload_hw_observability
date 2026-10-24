package main

import (
	"bytes"
	"encoding/json/v2"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/grafana/grafana-foundation-sdk/go/common"
	"github.com/grafana/grafana-foundation-sdk/go/dashboard"
	"github.com/grafana/grafana-foundation-sdk/go/prometheus"
	"github.com/grafana/grafana-foundation-sdk/go/timeseries"
	"github.com/grafana/grafana-foundation-sdk/go/units"
)

type ServiceAccountResponse struct {
	ID int64 `json:"id"`
}

type TokenResponse struct {
	Key string `json:"key"`
}

func getGrafanaToken(grafanaURL string) string {
	token := os.Getenv("GRAFANA_TOKEN")
	fmt.Println(token)
	if token != "" {
		return token
	}

	adminUser := os.Getenv("GRAFANA_ADMIN_USER")
	if adminUser == "" {
		adminUser = "admin"
	}
	adminPass := os.Getenv("GRAFANA_ADMIN_PASSWORD")
	if adminPass == "" {
		adminPass = "admin"
	}
	saPayload := []byte(`{"name":"generator", "role": "Admin"}`)
	req, _ := http.NewRequest("POST", grafanaURL+"/api/serviceaccounts", bytes.NewBuffer(saPayload))
	req.Header.Set("Content-Type", "application/json")
	req.SetBasicAuth(adminUser, adminPass)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	defer resp.Body.Close()

	var ID int64
	if resp.StatusCode == http.StatusConflict {
		ID = 1
	} else {
		body, _ := io.ReadAll(resp.Body)
		var saResp ServiceAccountResponse
		json.Unmarshal(body, &saResp)
		ID = saResp.ID
	}

	tokenPayload := []byte(`{"name":"generator-token"}`)
	req, _ = http.NewRequest("POST", fmt.Sprintf("%s/api/serviceaccounts/%d/tokens", grafanaURL, ID), bytes.NewBuffer(tokenPayload))
	req.Header.Set("Content-Type", "application/json")
	req.SetBasicAuth(adminUser, adminPass)

	resp, err = client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)
	var tokenResp TokenResponse
	json.Unmarshal(body, &tokenResp)

	if tokenResp.Key == "" {
		log.Fatal(string(body))
	}
	return tokenResp.Key
}

func main() {
	grafanaURL := os.Getenv("GRAFANA_URL")
	if grafanaURL == "" {
		grafanaURL = "http://grafana:3000"
	}

	token := getGrafanaToken(grafanaURL)
	err := os.Setenv("GRAFANA_TOKEN", token)
	if err != nil {
		log.Fatal(err)
	}

	client := &http.Client{}
	crudURL := os.Getenv("CRUD_URL")
	if crudURL == "" {
		crudURL = "http://crud:3000"
	}
	resp, err := client.Get(crudURL + "/endpoints")
	if err != nil {
		log.Fatal(err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		log.Fatal(err)
	}

	methods := make([]string, 0)
	err = json.Unmarshal(body, &methods)

	datasource := dashboard.DataSourceRef{
		Type: new("prometheus"),
		Uid:  new("victoriametrics-id"),
	}

	builder := dashboard.NewDashboardBuilder("CRUD Service Dashboard").
		Uid("CRUD-dashboard").
		Refresh("1m").
		Time("now-30m", "now").
		Timezone(common.TimeZoneBrowser).
		Readonly()

	datacenterVar := dashboard.NewQueryVariableBuilder("datacenter").
		Label("Datacenter").
		Datasource(datasource).
		Query(dashboard.StringOrMap{
			String: new("label_values(http_timer_golden_metrics_seconds_count, datacenter)"),
		}).
		IncludeAll(true).
		Multi(true).
		AllValue(".*")

	podVar := dashboard.NewQueryVariableBuilder("pod").
		Label("Pod").
		Datasource(datasource).
		Query(dashboard.StringOrMap{
			String: new(`label_values(http_timer_golden_metrics_seconds_count{datacenter=~"$datacenter"}, pod)`),
		}).
		IncludeAll(true).
		Multi(true).
		AllValue(".*")

	builder.
		WithVariable(datacenterVar).
		WithVariable(podVar)

	for _, method := range methods {
		info := strings.Split(method, " ")
		builder.WithRow(dashboard.NewRowBuilder(method).
			WithPanel(
				timeseries.NewPanelBuilder().
					GridPos(dashboard.GridPos{
						H:      8,
						W:      8,
						X:      0,
						Y:      0,
						Static: nil,
					},
					).
					Datasource(datasource).
					Title("Latency").
					Unit(units.Seconds).
					Min(0).
					WithTarget(
						prometheus.NewDataqueryBuilder().
							Expr(fmt.Sprintf("histogram_quantile(0.50, sum(rate(http_timer_golden_metrics_seconds_bucket{endpoint=\"%s\", datacenter=~\"$datacenter\", pod=~\"$pod\"}[$__rate_interval])) by (le, endpoint))", info[1])).
							LegendFormat("p50")).
					WithTarget(
						prometheus.NewDataqueryBuilder().
							Expr(fmt.Sprintf("histogram_quantile(0.75, sum(rate(http_timer_golden_metrics_seconds_bucket{endpoint=\"%s\", datacenter=~\"$datacenter\", pod=~\"$pod\"}[$__rate_interval])) by (le, endpoint))", info[1])).
							LegendFormat("p75")).
					WithTarget(
						prometheus.NewDataqueryBuilder().
							Expr(fmt.Sprintf("histogram_quantile(0.90, sum(rate(http_timer_golden_metrics_seconds_bucket{endpoint=\"%s\", datacenter=~\"$datacenter\", pod=~\"$pod\"}[$__rate_interval])) by (le, endpoint))", info[1])).
							LegendFormat("p90")).
					WithTarget(
						prometheus.NewDataqueryBuilder().
							Expr(fmt.Sprintf("histogram_quantile(0.95, sum(rate(http_timer_golden_metrics_seconds_bucket{endpoint=\"%s\", datacenter=~\"$datacenter\", pod=~\"$pod\"}[$__rate_interval])) by (le, endpoint))", info[1])).
							LegendFormat("p95")).
					WithTarget(
						prometheus.NewDataqueryBuilder().
							Expr(fmt.Sprintf("histogram_quantile(0.99, sum(rate(http_timer_golden_metrics_seconds_bucket{endpoint=\"%s\", datacenter=~\"$datacenter\", pod=~\"$pod\"}[$__rate_interval])) by (le, endpoint))", info[1])).
							LegendFormat("p99")),
			).WithPanel(
			timeseries.NewPanelBuilder().
				GridPos(dashboard.GridPos{
					H:      8,
					W:      8,
					X:      0,
					Y:      0,
					Static: nil,
				},
				).
				Datasource(datasource).
				Title("RPS").
				Unit(units.RequestsPerSecond).
				Min(0).
				WithTarget(
					prometheus.NewDataqueryBuilder().
						Expr(fmt.Sprintf("sum(rate(http_counter_golden_metrics_total{endpoint=\"%s\", datacenter=~\"$datacenter\", pod=~\"$pod\"}[$__rate_interval]))", info[1])).
						LegendFormat("RPS")),
		).WithPanel(
			timeseries.NewPanelBuilder().
				GridPos(dashboard.GridPos{
					H:      8,
					W:      8,
					X:      0,
					Y:      0,
					Static: nil,
				},
				).
				Datasource(datasource).
				Title("Errors Rate").
				Unit(units.RequestsPerSecond).
				Min(0).
				WithTarget(
					prometheus.NewDataqueryBuilder().
						Expr(fmt.Sprintf("sum(rate(http_counter_golden_metrics_total{endpoint=\"%s\", status=\"error\"}[$__rate_interval]))", info[1])).
						LegendFormat("Errors"),
				),
		),
		)
	}

	sampleDashboard, err := builder.Build()
	if err != nil {
		log.Fatal(err)
	}

	payload := map[string]any{
		"dashboard": sampleDashboard,
		"overwrite": true,
		"message":   "Generated by Go script",
	}

	dashboardJson, err := json.Marshal(payload)
	if err != nil {
		log.Fatal("Ошибка маршалинга JSON:", err)
	}

	req, _ := http.NewRequest("POST", grafanaURL+"/api/dashboards/db", bytes.NewBuffer(dashboardJson))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+token)
	resp, err = client.Do(req)

	if err != nil {
		log.Fatal("Ошибка сети при отправке дашборда:", err)
	}
	defer resp.Body.Close()

	body, _ = io.ReadAll(resp.Body)

	if resp.StatusCode == http.StatusOK {
		log.Println("Дашборд успешно создан!")
		log.Printf("Ответ Grafana: %s\n", string(body))
	} else {
		log.Fatalf("❌ Ошибка создания дашборда. Код: %d, Ответ: %s", resp.StatusCode, string(body))
	}
}
