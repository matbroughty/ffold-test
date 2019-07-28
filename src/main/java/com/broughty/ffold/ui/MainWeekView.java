package com.broughty.ffold.ui;

import com.broughty.ffold.entity.Week;
import com.broughty.ffold.repository.SeasonRepository;
import com.broughty.ffold.repository.WeekRepository;
import com.vaadin.flow.component.button.Button;
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



@Route(value = "")
public class MainWeekView extends VerticalLayout implements HasUrlParameter<String> {
    private static final Logger log = LoggerFactory.getLogger(MainWeekView.class);
    private final WeekRepository weekRepository;

    private final SeasonRepository seasonRepository;

    private final WeekEditor editor;

    final Grid<Week> grid;

    final TextField filter;

    private final Button addNewBtn;

    private boolean isAdmin = false;

    private String playerGroupStr = null;

    Label label = new Label("Four Fold Competition ");

    public MainWeekView(WeekRepository weekRepository, SeasonRepository seasonRepository, WeekEditor editor) {
        log.info("In MainWeekView with isAdmin = {} and player group {}", isAdmin, playerGroupStr);
        this.weekRepository = weekRepository;
        this.seasonRepository = seasonRepository;
        this.editor = editor;
        this.grid = new Grid<>(Week.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New Week ", VaadinIcon.PLUS.create());


        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);



        add(label,actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("id", "weekNumber");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

        filter.setPlaceholder("Filter by week Number");

        // Hook logic to components

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listWeeks(e.getValue()));

        // Connect selected Week to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> editor.editWeek(e.getValue()));

        // Instantiate and edit new Week the new button is clicked
        addNewBtn.addClickListener(e -> editor.editWeek(new Week(5)));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listWeeks(filter.getValue());
        });

        // Initialize listing
        listWeeks(null);
    }

    void listWeeks(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(weekRepository.findCurrentWeeks());
        } else {
            grid.setItems(weekRepository.findCurrentWeeksForPlayerGroup(playerGroupStr));
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

        if(!isAdmin){
            editor.setVisible(false);
            editor.setEnabled(false);
            addNewBtn.setEnabled(false);
        }else{
            editor.setVisible(true);
            addNewBtn.setEnabled(true);
        }

        label.setText("Four Fold Competition " + playerGroupStr);
        label.setHeight("20%");
        label.setWidth("100%");
        if(StringUtils.isNotBlank(playerGroupStr)) {
            grid.setItems(weekRepository.findCurrentWeeksForPlayerGroup(playerGroupStr));
        }

    }

}