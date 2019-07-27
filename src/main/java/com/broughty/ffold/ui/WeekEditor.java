package com.broughty.ffold.ui;

import com.broughty.ffold.entity.Week;
import com.broughty.ffold.repository.WeekRepository;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;


@SpringComponent
@UIScope
public class WeekEditor extends VerticalLayout implements KeyNotifier {

    private final WeekRepository repository;

    /**
     * The currently edited week
     */
    private Week week;

    /* Fields to edit properties in Week entity */
    TextField weekNumber = new TextField("Week Number");


    /* Action buttons */
    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<Week> binder = new Binder<>(Week.class);
    private ChangeHandler changeHandler;

    @Autowired
    public WeekEditor(WeekRepository repository) {
        this.repository = repository;
        binder.forField(weekNumber).withConverter(new StringToIntegerConverter(""))
                .bind(Week::getWeekNumber, Week::setWeekNumber);


        //binder.forField()

        add(weekNumber, actions);

        // bind using naming convention
        binder.bindInstanceFields(this);

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> save());
        ConfirmDialog dialog = new ConfirmDialog("Deleting Week",
                "Are you sure you want to delete week?", "OK", e -> deleteWeek());
        delete.addClickListener(e -> dialog.open());
        cancel.addClickListener(e -> cancel());
        setVisible(false);
    }


    void deleteWeek() {
        repository.delete(week);
        changeHandler.onChange();
    }

    void save() {
        repository.save(week);
        changeHandler.onChange();
    }
    void cancel() {
        changeHandler.onChange();
    }


    public interface ChangeHandler {
        void onChange();
    }

    public final void editWeek(Week c) {
        if (c == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = c.getId() != null;
        if (persisted) {
            // Find fresh entity for editing
            week = repository.findById(c.getId()).get();
        } else {
            week = c;
        }
        cancel.setVisible(persisted);

        // Bind week properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(week);

        setVisible(true);

        // Focus first name initially
        weekNumber.focus();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

}