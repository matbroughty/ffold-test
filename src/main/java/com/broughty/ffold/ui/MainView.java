package com.broughty.ffold.ui;

import com.broughty.ffold.entity.Week;
import com.broughty.ffold.repository.WeekRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;

@Route
public class MainView extends VerticalLayout {

	private final WeekRepository repo;

	private final CustomerEditor editor;

	final Grid<Week> grid;

	final TextField filter;

	private final Button addNewBtn;

	public MainView(WeekRepository repo, CustomerEditor editor) {
		this.repo = repo;
		this.editor = editor;
		this.grid = new Grid<>(Week.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New customer", VaadinIcon.PLUS.create());

		// build layout
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		add(actions, grid, editor);

		grid.setHeight("300px");
		grid.setColumns("id", "weekNumber");
		grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

		filter.setPlaceholder("Filter by week Number");

		// Hook logic to components

		// Replace listing with filtered content when user changes filter
		filter.setValueChangeMode(ValueChangeMode.EAGER);
		filter.addValueChangeListener(e -> listCustomers(e.getValue()));

		// Connect selected Week to editor or hide if none is selected
		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editCustomer(e.getValue());
		});

		// Instantiate and edit new Week the new button is clicked
		addNewBtn.addClickListener(e -> editor.editCustomer(new Week(5)));

		// Listen changes made by the editor, refresh data from backend
		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listCustomers(filter.getValue());
		});

		// Initialize listing
		listCustomers(null);
	}

	// tag::listCustomers[]
	void listCustomers(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			grid.setItems(repo.findAll());
		}
		else {
			grid.setItems(repo.findAll());
		}
	}
	// end::listCustomers[]

}