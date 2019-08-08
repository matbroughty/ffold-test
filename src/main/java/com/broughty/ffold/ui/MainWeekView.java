package com.broughty.ffold.ui;

import com.broughty.ffold.repository.PlayerGroupRepository;
import com.broughty.ffold.repository.SeasonRepository;
import com.broughty.ffold.repository.WeekRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


@Route(value = "")
public class MainWeekView extends VerticalLayout implements HasUrlParameter<String> {
    private static final Logger log = LoggerFactory.getLogger(MainWeekView.class);
    final Grid<Map<String, Object>> grid;
    final TextField filter;
    private final WeekRepository weekRepository;
    private final SeasonRepository seasonRepository;
    private final WeekEditor editor;
    private final Button addNewBtn;
    Label label = new Label("Four Fold Competition ");
    Chart chart;
    private boolean isAdmin = false;
    private String playerGroupStr = null;
    private PlayerGroupRepository playerGroupRepository;
    private WeekRepository customWeekRepository;
    List<Map<String, Object>> weeks; // the data for the grid

    public MainWeekView(WeekRepository weekRepository, SeasonRepository seasonRepository, WeekEditor editor, PlayerGroupRepository playerGroupRepository, WeekRepository customWeekRepository) {
        this.playerGroupRepository = playerGroupRepository;
        this.customWeekRepository = customWeekRepository;
        log.info("In MainWeekView with isAdmin = {} and player group {}", isAdmin, playerGroupStr);
        this.weekRepository = weekRepository;
        this.seasonRepository = seasonRepository;
        this.editor = editor;
        this.grid = new Grid<>();
        this.filter = new TextField();
        this.addNewBtn = new Button("New Week ", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);

        //chart = getChart();
        chart = getMultiChart();
        add(chart, label, actions, grid, editor);

        grid.setHeight("300px");
        filter.setPlaceholder("Filter by week Number");

        // Hook logic to components

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listWeeks(e.getValue()));

        // Connect selected Week to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> editor.editWeek(e.getValue()));

        // Instantiate and edit new Week the new button is clicked
        addNewBtn.addClickListener(e -> {
            editor.editWeek(customWeekRepository.createNextWeekForPlayerGroupMap(playerGroupStr));
        });

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listWeeks(filter.getValue());
        });

        // Initialize listing
        listWeeks(null);
    }


    void listWeeks(String filterText) {

        log.info("Filtering on {}", filterText);
        if (!StringUtils.isEmpty(filterText)) {
            grid.setItems(weekRepository.findCurrentSeasonsWeeksForPlayerGroupMap(playerGroupStr, filterText));
        } else {
            grid.setItems(weekRepository.findCurrentSeasonsWeeksForPlayerGroupMap(playerGroupStr, null));
        }
    }


    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        log.info("The parameter is {}", parameter);
        if (StringUtils.contains(parameter, "/")) {
            playerGroupStr = StringUtils.substringBefore(parameter, "/");
            if (StringUtils.substringAfterLast(StringUtils.trimToEmpty(parameter), "/").equalsIgnoreCase("admin")) {
                log.info("In Admin Mode");
                isAdmin = true;
            } else {
                isAdmin = false;
            }
        } else {
            playerGroupStr = StringUtils.trim(parameter);
            isAdmin = false;
        }

        log.info("Player Group = {} and isAdmin = {}", parameter, isAdmin);

        if (!isAdmin) {
            editor.setVisible(false);
            editor.setEnabled(false);
            addNewBtn.setEnabled(false);
            addNewBtn.setVisible(false);
        } else {
            editor.setVisible(true);
            editor.setEnabled(true);
            addNewBtn.setEnabled(true);
            addNewBtn.setVisible(true);

        }

        //chart = getChart();
        chart = getMultiChart();
        label.setText("Four Fold Competition " + playerGroupStr);
        label.setHeight("20%");
        label.setWidth("100%");
        if (StringUtils.isNotBlank(playerGroupStr)) {
            weeks = weekRepository.findCurrentSeasonsWeeksForPlayerGroupMap(playerGroupStr, null);

            Map<String, Object> s = weeks.get(0);
            for (Map.Entry<String, Object> entry : s.entrySet()) {
                log.info("add column entry {}->{}", entry.getKey(), entry.getValue());
                if(!StringUtils.endsWith(entry.getKey(), "id")) {
                    grid.addColumn(h -> h.get(entry.getKey())).setHeader(new Label(entry.getKey()));
                }
            }
            grid.setItems(weeks);
            grid.getDataProvider().refreshAll();

        }

    }

    //todo use grdi data to produce charts
    protected Chart getMultiChart() {
        final Chart chart = new Chart(ChartType.COLUMN);
        chart.setId("chart");

        final Configuration conf = chart.getConfiguration();

        conf.setTitle("Four Fold " + StringUtils.trimToEmpty(playerGroupStr));
        conf.setSubTitle(playerGroupStr);
        conf.getLegend().setEnabled(false);

        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Total Winnings");
        conf.addyAxis(y);

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setDataLabels(new DataLabels(true));

        conf.setPlotOptions(column);

        DataSeries regionsSeries = new DataSeries();
        regionsSeries.setName("Players");
        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        plotOptionsColumn.setColorByPoint(true);
        regionsSeries.setPlotOptions(plotOptionsColumn);

        //weeks.stream().re


        DataSeriesItem regionItem = new DataSeriesItem(
                "Latin America and Carribean", 60);
        DataSeries countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Latin America and Carribean Countries");

        DataSeriesItem countryItem = new DataSeriesItem("Costa Rica", 64);
        DataSeries detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Costa Rica");
        String[] categories = new String[]{"Life Expectancy",
                "Well-being (0-10)", "Footprint (gha/capita)"};
        Number[] ys = new Number[]{79.3, 7.3, 2.5};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Colombia", 59.8);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Colombia");
        ys = new Number[]{73.7, 6.4, 1.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Belize", 59.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Belize");
        ys = new Number[]{76.1, 6.5, 2.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("El Salvador", 58.9);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details El Salvador");
        ys = new Number[]{72.2, 6.7, 2.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("Western Nations", 50);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Western Nations Countries");

        countryItem = new DataSeriesItem("New Zealand", 51.6);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details New Zealand");
        ys = new Number[]{80.7, 7.2, 4.3};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Norway", 51.4);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Norway");
        ys = new Number[]{81.1, 7.6, 4.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Switzerland", 50.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Switzerland");
        ys = new Number[]{82.3, 7.5, 5.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("United Kingdom", 47.9);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details United Kingdom");
        ys = new Number[]{80.2, 7.0, 4.7};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("Middle East and North Africa", 53);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Middle East and North Africa Countries");

        countryItem = new DataSeriesItem("Israel", 55.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Israel");
        ys = new Number[]{81.6, 7.4, 4.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Algeria", 52.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Algeria");
        ys = new Number[]{73.1, 5.2, 1.6};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Jordan", 51.7);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Jordan");
        ys = new Number[]{73.4, 5.7, 2.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Palestine", 51.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Palestine");
        ys = new Number[]{72.8, 4.8, 1.4};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("Sub-Saharan Africa", 42);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Sub-Saharan Africa Countries");

        countryItem = new DataSeriesItem("Madagascar", 51.6);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Madagascar");
        ys = new Number[]{66.7, 4.6, 1.2};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Malawi", 42.5);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Malawi");
        ys = new Number[]{54.2, 5.1, 0.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Ghana", 40.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Ghana");
        ys = new Number[]{64.2, 4.6, 1.7};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Ethiopia", 39.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Ethiopia");
        ys = new Number[]{59.3, 4.4, 1.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("South Asia", 53);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("South Asia Countries");

        countryItem = new DataSeriesItem("Bangladesh", 56.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Bangladesh");
        ys = new Number[]{68.9, 5.0, 0.7};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Pakistan", 54.1);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Pakistan");
        ys = new Number[]{65.4, 5.3, 0.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("India", 50.9);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details India");
        ys = new Number[]{65.4, 5.0, 0.9};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Sri Lanka", 51.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Sri Lanka");
        ys = new Number[]{74.9, 4.2, 1.2};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("East Asia", 55);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("East Asia Countries");

        countryItem = new DataSeriesItem("Vietnam", 60.4);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Vietnam");
        ys = new Number[]{75.2, 5.8, 1.4};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Indonesia", 55.5);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Indonesia");
        ys = new Number[]{69.4, 5.5, 1.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Thailand", 53.5);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Thailand");
        ys = new Number[]{74.1, 6.2, 2.4};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Philippines", 52.4);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Philippines");
        ys = new Number[]{68.7, 4.9, 1.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        conf.addSeries(regionsSeries);

        return chart;
    }



    protected Chart getCopyMultiChart() {
        final Chart chart = new Chart(ChartType.COLUMN);
        chart.setId("chart");

        final Configuration conf = chart.getConfiguration();

        conf.setTitle("Global happiness index");
        conf.setSubTitle("Source: www.happyplanetindex.org");
        conf.getLegend().setEnabled(false);

        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Total percent market share");
        conf.addyAxis(y);

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setDataLabels(new DataLabels(true));

        conf.setPlotOptions(column);

        DataSeries regionsSeries = new DataSeries();
        regionsSeries.setName("Regions");
        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        plotOptionsColumn.setColorByPoint(true);
        regionsSeries.setPlotOptions(plotOptionsColumn);

        DataSeriesItem regionItem = new DataSeriesItem(
                "Latin America and Carribean", 60);
        DataSeries countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Latin America and Carribean Countries");

        DataSeriesItem countryItem = new DataSeriesItem("Costa Rica", 64);
        DataSeries detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Costa Rica");
        String[] categories = new String[]{"Life Expectancy",
                "Well-being (0-10)", "Footprint (gha/capita)"};
        Number[] ys = new Number[]{79.3, 7.3, 2.5};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Colombia", 59.8);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Colombia");
        ys = new Number[]{73.7, 6.4, 1.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Belize", 59.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Belize");
        ys = new Number[]{76.1, 6.5, 2.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("El Salvador", 58.9);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details El Salvador");
        ys = new Number[]{72.2, 6.7, 2.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("Western Nations", 50);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Western Nations Countries");

        countryItem = new DataSeriesItem("New Zealand", 51.6);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details New Zealand");
        ys = new Number[]{80.7, 7.2, 4.3};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Norway", 51.4);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Norway");
        ys = new Number[]{81.1, 7.6, 4.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Switzerland", 50.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Switzerland");
        ys = new Number[]{82.3, 7.5, 5.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("United Kingdom", 47.9);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details United Kingdom");
        ys = new Number[]{80.2, 7.0, 4.7};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("Middle East and North Africa", 53);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Middle East and North Africa Countries");

        countryItem = new DataSeriesItem("Israel", 55.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Israel");
        ys = new Number[]{81.6, 7.4, 4.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Algeria", 52.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Algeria");
        ys = new Number[]{73.1, 5.2, 1.6};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Jordan", 51.7);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Jordan");
        ys = new Number[]{73.4, 5.7, 2.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Palestine", 51.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Palestine");
        ys = new Number[]{72.8, 4.8, 1.4};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("Sub-Saharan Africa", 42);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("Sub-Saharan Africa Countries");

        countryItem = new DataSeriesItem("Madagascar", 51.6);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Madagascar");
        ys = new Number[]{66.7, 4.6, 1.2};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Malawi", 42.5);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Malawi");
        ys = new Number[]{54.2, 5.1, 0.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Ghana", 40.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Ghana");
        ys = new Number[]{64.2, 4.6, 1.7};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Ethiopia", 39.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Ethiopia");
        ys = new Number[]{59.3, 4.4, 1.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("South Asia", 53);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("South Asia Countries");

        countryItem = new DataSeriesItem("Bangladesh", 56.3);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Bangladesh");
        ys = new Number[]{68.9, 5.0, 0.7};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Pakistan", 54.1);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Pakistan");
        ys = new Number[]{65.4, 5.3, 0.8};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("India", 50.9);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details India");
        ys = new Number[]{65.4, 5.0, 0.9};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Sri Lanka", 51.2);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Sri Lanka");
        ys = new Number[]{74.9, 4.2, 1.2};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        regionItem = new DataSeriesItem("East Asia", 55);

        countriesSeries = new DataSeries("Countries");
        countriesSeries.setId("East Asia Countries");

        countryItem = new DataSeriesItem("Vietnam", 60.4);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Vietnam");
        ys = new Number[]{75.2, 5.8, 1.4};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Indonesia", 55.5);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Indonesia");
        ys = new Number[]{69.4, 5.5, 1.1};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Thailand", 53.5);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Thailand");
        ys = new Number[]{74.1, 6.2, 2.4};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        countryItem = new DataSeriesItem("Philippines", 52.4);
        detailsSeries = new DataSeries("Details");
        detailsSeries.setId("Details Philippines");
        ys = new Number[]{68.7, 4.9, 1.0};
        detailsSeries.setData(categories, ys);
        countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

        regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

        conf.addSeries(regionsSeries);

        return chart;
    }

}