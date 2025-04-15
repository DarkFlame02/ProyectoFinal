package dam.dad.app.controller;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import dam.dad.app.db.DatabaseManager;
import dam.dad.app.model.Reparacion;
import dam.dad.app.model.Taller;
import dam.dad.app.model.Valoracion;
import dam.dad.app.model.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class InformesController {
    
    @FXML
    private VBox costosContainer;
    
    @FXML
    private PieChart valoracionesPieChart;
    
    @FXML
    private LineChart<String, Number> historialLineChart;
    
    private DatabaseManager dbManager;
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
    }
    
    // Carga todos los datos para los informes
    public void cargarInformes() {
        cargarGraficoCostos();
        cargarGraficoValoraciones();
        cargarGraficoHistorial();
    }
    
    // Carga el gráfico de costos por vehículo
    private void cargarGraficoCostos() {
        try {
            costosContainer.getChildren().clear();
            
            List<Vehiculo> vehiculos = dbManager.getVehiculosByUsuario();
            
            List<VehiculoCosto> vehiculosCosto = new java.util.ArrayList<>();
            double maxCosto = 0;
            
            for (Vehiculo vehiculo : vehiculos) {
                double costoTotal = 0;
                if (vehiculo.getReparaciones() != null) {
                    for (Reparacion reparacion : vehiculo.getReparaciones()) {
                        costoTotal += reparacion.getCosto();
                    }
                }
                
                if (costoTotal > 0) {
                    String etiqueta = vehiculo.getMarca() + " " + vehiculo.getModelo() + " (" + vehiculo.getMatricula() + ")";
                    vehiculosCosto.add(new VehiculoCosto(vehiculo, etiqueta, costoTotal));
                    maxCosto = Math.max(maxCosto, costoTotal);
                }
            }
            
            vehiculosCosto.sort((v1, v2) -> Double.compare(v2.costo, v1.costo));
            
            if (vehiculosCosto.isEmpty()) {
                Label noDataLabel = new Label("No hay datos de costos disponibles");
                noDataLabel.setStyle("-fx-font-style: italic;");
                noDataLabel.setPadding(new Insets(20));
                costosContainer.getChildren().add(noDataLabel);
                return;
            }
            
            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(8);
            gridPane.setPadding(new Insets(10));
            
            String[] colores = {
                "#4682B4",
                "#1E90FF",
                "#6495ED",
                "#87CEEB",
                "#4169E1"
            };
            
            for (int i = 0; i < vehiculosCosto.size(); i++) {
                VehiculoCosto vc = vehiculosCosto.get(i);
                
                Label vehiculoLabel = new Label(vc.etiqueta);
                vehiculoLabel.setPrefWidth(200);
                vehiculoLabel.setWrapText(true);
                gridPane.add(vehiculoLabel, 0, i);
                
                double porcentajeAncho = vc.costo / maxCosto;
                Rectangle barra = new Rectangle(porcentajeAncho * 400, 25);
                String colorIndex = colores[i % colores.length];
                barra.setFill(Color.web(colorIndex));
                barra.setArcWidth(5);
                barra.setArcHeight(5);
                HBox barraContainer = new HBox(barra);
                barraContainer.setPadding(new Insets(0, 5, 0, 0));
                gridPane.add(barraContainer, 1, i);
                
                Label costoLabel = new Label(String.format("%.2f €", vc.costo));
                costoLabel.setStyle("-fx-font-weight: bold;");
                gridPane.add(costoLabel, 2, i);
            }
            
            GridPane.setHgrow(gridPane.getChildren().get(1), Priority.ALWAYS);
            
            costosContainer.getChildren().add(gridPane);
            
            VBox.setVgrow(gridPane, Priority.ALWAYS);
            
        } catch (Exception e) {
            mostrarAlerta("Error al cargar gráfico de costos", e.getMessage());
            e.printStackTrace();
        }
    }

    private class VehiculoCosto {
        Vehiculo vehiculo;
        String etiqueta;
        double costo;
        
        VehiculoCosto(Vehiculo vehiculo, String etiqueta, double costo) {
            this.vehiculo = vehiculo;
            this.etiqueta = etiqueta;
            this.costo = costo;
        }
    }
    
    // Carga el gráfico circular con la distribución de valoraciones por taller
    private void cargarGraficoValoraciones() {
        try {
            // Obtener talleres con valoraciones
            List<Taller> talleres = dbManager.getAllTalleres();
            
            // Preparar datos para el gráfico
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            for (Taller taller : talleres) {
                // Solo añadir talleres con valoración
                if (taller.getValoracionMedia() > 0) {
                    String label = taller.getNombre() + " (" + taller.getValoracionMedia() + ")";
                    pieChartData.add(new PieChart.Data(label, taller.getValoracionMedia()));
                }
            }
            
            // Actualizar gráfico
            valoracionesPieChart.setData(pieChartData);
            
            // Configurar etiquetas para mejorar visualización
            valoracionesPieChart.setLabelsVisible(false);
            valoracionesPieChart.setLegendVisible(true);
            valoracionesPieChart.setLegendSide(javafx.geometry.Side.RIGHT);
            
            // Aplicar estilo personalizado 
            valoracionesPieChart.setClockwise(true);
            valoracionesPieChart.setStartAngle(90);
            
        } catch (Exception e) {
            mostrarAlerta("Error al cargar gráfico de valoraciones", e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Carga el gráfico de líneas con el historial de costos por mes
    private void cargarGraficoHistorial() {
        try {
            // Obtener todos los vehículos del usuario actual
            List<Vehiculo> vehiculos = dbManager.getVehiculosByUsuario();
            
            // Obtener todas las reparaciones
            List<Reparacion> todasReparaciones = vehiculos.stream()
                    .filter(v -> v.getReparaciones() != null)
                    .flatMap(v -> v.getReparaciones().stream())
                    .collect(Collectors.toList());
            
            // Preparar mapa para agrupar por mes
            Map<Month, Double> costosPorMes = new HashMap<>();
            
            // Año actual
            int anioActual = LocalDate.now().getYear();
            
            // Calcular costos por mes para el año actual
            for (Reparacion reparacion : todasReparaciones) {
                LocalDate fecha = reparacion.getFechaReparacion();
                if (fecha != null && fecha.getYear() == anioActual) {
                    Month mes = fecha.getMonth();
                    costosPorMes.put(mes, costosPorMes.getOrDefault(mes, 0.0) + reparacion.getCosto());
                }
            }
            
            // Preparar la serie de datos
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Costos mensuales " + anioActual);
            
            // Añadir datos para todos los meses (incluso los que no tienen reparaciones)
            for (Month mes : Month.values()) {
                String nombreMes = mes.getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
                double costo = costosPorMes.getOrDefault(mes, 0.0);
                series.getData().add(new XYChart.Data<>(nombreMes, costo));
            }
            
            // Limpiar gráfico anterior
            historialLineChart.getData().clear();
            // Añadir nueva serie
            historialLineChart.getData().add(series);
            
        } catch (Exception e) {
            mostrarAlerta("Error al cargar gráfico de historial", e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Muestra una alerta de error
    private void mostrarAlerta(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 