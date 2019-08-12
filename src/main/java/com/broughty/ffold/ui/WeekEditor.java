package com.broughty.ffold.ui;

import com.broughty.ffold.repository.WeekRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@SpringComponent
@UIScope
public class WeekEditor extends VerticalLayout implements KeyNotifier {
    private static final Logger log = LoggerFactory.getLogger(WeekEditor.class);
    @Autowired
    private final WeekRepository repository;
    /* Action buttons */
    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);
    Binder<Map<String, Object>> binder = new Binder<>();
    /**
     * The currently edited week - needs to be persisted/used for update
     */
    private Map<String, Object> weekDetails;
    private ChangeHandler changeHandler;


    @Autowired
    public WeekEditor(WeekRepository repository) {
        this.repository = repository;

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> save());
        ConfirmDialog dialog = new ConfirmDialog("Deleting Week",
                "Are you sure you want to delete week?", "OK", e -> deleteWeek(), "Nope", e -> cancel());
        delete.addClickListener(e -> dialog.open());
        cancel.addClickListener(e -> cancel());
        setVisible(false);
    }

    /**
     * Create a textfield
     * - add it to the layout
     * - bind the Map value to the textfield
     */
    private TextField createTextField(Map<String, Object> week, String key) {
        TextField tf = new TextField();
        tf.setWidth("250px");
        tf.setTitle(key);
        tf.setLabel(key);

        //bind element
        binder.forField(tf).bind(// getter
                list -> {
                    return week.get(key) != null ? week.get(key).toString() : null;
                },
                //setter
                (list, fieldValue) -> {
                    list.put(key, fieldValue);
                });


        // no editing of id's
        if (StringUtils.endsWith(key, "id")) {
            tf.setEnabled(false);
        }

        return tf;
    }


    void deleteWeek() {
        repository.delete(weekDetails);
        changeHandler.onChange();
    }

    void save() {
        repository.save(weekDetails);
        changeHandler.onChange();
    }

    void cancel() {
        changeHandler.onChange();
    }

    // TODO - take map and create week to persist
    public final void editWeek(Map<String, Object> week) {
        binder.setBean(null);
        removeAll();

        HorizontalLayout form = new HorizontalLayout();

        List<Component> components = new ArrayList<>();
        if (week != null) {
            week.forEach((k, v) -> {
                log.info("adding component for key {} and value {}", k, v);
                components.add(createTextField(week, k));
            });
        }

        form.add(components.toArray(new Component[components.size()]));
        add(form, actions);

        // bind using naming convention
        binder.bindInstanceFields(this);

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());


        if (week == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = week.get("week_id") != null;
        log.info("week is persisted = {}", persisted);
        cancel.setVisible(persisted);

        // Bind week properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(week);
        weekDetails = week;

        setVisible(true);

    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

    public interface ChangeHandler {
        void onChange();
    }

}