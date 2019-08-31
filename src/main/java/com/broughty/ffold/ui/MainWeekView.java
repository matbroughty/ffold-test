package com.broughty.ffold.ui;

import com.broughty.ffold.repository.PlayerGroupRepository;
import com.broughty.ffold.repository.PlayerTotals;
import com.broughty.ffold.repository.SeasonRepository;
import com.broughty.ffold.repository.WeekRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.theme.lumo.Lumo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
    Chart chart = new Chart(ChartType.COLUMN);
    Chart chart2 = new Chart(ChartType.LINE);
    List<Map<String, Object>> weeks; // the data for the grid
    private boolean isAdmin = false;
    private Button bannerButton = new Button(new Icon(VaadinIcon.CASH));
    private String playerGroupStr = null;
    private PlayerGroupRepository playerGroupRepository;
    private WeekRepository customWeekRepository;

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

        bannerButton.setThemeName(Lumo.DARK);
        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);

        getMultiChart();
        getLineChart();
        add(bannerButton, chart, chart2, label, actions, grid, editor);
        setHorizontalComponentAlignment(Alignment.STRETCH, bannerButton);

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
            getMultiChart();
            getLineChart();
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

        getMultiChart();
        getLineChart();
        label.setText("Four Fold Competition " + playerGroupStr);
        label.setHeight("20%");
        label.setWidth("100%");
        if (StringUtils.isNotBlank(playerGroupStr)) {
            weeks = weekRepository.findCurrentSeasonsWeeksForPlayerGroupMap(playerGroupStr, null);
            if (!weeks.isEmpty()) {
                Map<String, Object> s = weeks.get(0);
                for (Map.Entry<String, Object> entry : s.entrySet()) {
                    log.info("add column entry {}->{}", entry.getKey(), entry.getValue());
                    if (!StringUtils.endsWith(entry.getKey(), "id")) {
                        grid.addColumn(h -> h.get(entry.getKey())).setHeader(new Label(entry.getKey()));
                    }
                }
            }
            grid.setItems(weeks);
            grid.getDataProvider().refreshAll();

        }

    }


    protected Chart getMultiChart() {

        chart.setId("chart");

        final Configuration conf = chart.getConfiguration();

        conf.setTitle("Four Fold " + StringUtils.trimToEmpty(playerGroupStr));
        conf.setSubTitle(playerGroupStr);
        conf.getLegend().setEnabled(false);

        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Total Winnings (GBP)");
        conf.addyAxis(y);

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setDataLabels(new DataLabels(true));

        conf.setPlotOptions(column);

        DataSeries playerSeries = new DataSeries();
        playerSeries.setName("Players");
        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        plotOptionsColumn.setColorByPoint(true);
        playerSeries.setPlotOptions(plotOptionsColumn);

        Map<String, PlayerTotals> playerTotals = weekRepository.buildPlayerTotalMap(playerGroupStr);
        playerTotals.forEach((k, v) -> {
            DataSeriesItem playerItem = new DataSeriesItem(
                    k, v.getTotalWon());

            DataSeries playerWeekSeries = new DataSeries("Wins");
            playerWeekSeries.setId(k + " Wins");
            v.getWinningResults().forEach(pr -> {
                DataSeriesItem playerWeekItem = new DataSeriesItem(pr.getWeek().getWeekNumber(), pr.getWinnings());
                playerWeekSeries.add(playerWeekItem);
            });

            log.info("adding player item {} {}{} to player series {}", k, v, playerItem, playerSeries);
            playerSeries.addItemWithDrilldown(playerItem, playerWeekSeries);
        });


        conf.addSeries(playerSeries);
        chart.drawChart();

        return chart;
    }


    protected Chart getLineChart() {

        chart2.setId("chart2");

        Configuration configuration = chart2.getConfiguration();

        configuration.setTitle("Season Progress");
        configuration.setSubTitle(playerGroupStr);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle("Winnings (£)");

        configuration.getxAxis().setTitle("Weeks");



        Legend legend = configuration.getLegend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setVerticalAlign(VerticalAlign.MIDDLE);
        legend.setAlign(HorizontalAlign.RIGHT);

        //PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        //plotOptionsSeries.setPointStart(1);
        //configuration.setPlotOptions(plotOptionsSeries);

        Map<String, PlayerTotals> playerTotals = weekRepository.buildPlayerTotalMap(playerGroupStr);
        List<String> weekNumbers = weekRepository.findCurrentWeeksForPlayerGroup(playerGroupStr).stream().map(week-> week.getWeekNumber().toString()).collect(Collectors.toList());
        configuration.getxAxis().setCategories(weekNumbers.toArray(new String[weekNumbers.size()]));
        playerTotals.forEach((k, v) -> {

            ListSeries listSeries = new ListSeries();
            listSeries.setName(k);
            v.getWinningResults().forEach(res -> {
                        BigDecimal current = BigDecimal.ZERO;
                        if (listSeries.getData() != null && listSeries.getData().length > 0) {
                            current = (BigDecimal) listSeries.getData()[listSeries.getData().length - 1];
                        }
                        listSeries.addData(current.add(res.getWinnings()));
                    }
            );
            configuration.addSeries(listSeries);

        });
        chart2.drawChart();
        return chart2;

    }


}