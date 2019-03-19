package ir.nimcode.dolphin.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FormsUpdate  {

    @SerializedName("forms")
    private List<Form> forms ;

    @SerializedName("sheets")
    private List<Sheet> sheets ;

    @SerializedName("forms_sheets")
    private List<FormSheet> formsSheets ;

    @SerializedName("properties")
    private List<Property> properties ;

    @SerializedName("forms_sheets_properties")
    private List<FormSheetProperty> formsSheetsProperties ;

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public List<Sheet> getSheets() {
        return sheets;
    }

    public void setSheets(List<Sheet> sheets) {
        this.sheets = sheets;
    }

    public List<FormSheet> getFormsSheets() {
        return formsSheets;
    }

    public void setFormsSheets(List<FormSheet> formsSheets) {
        this.formsSheets = formsSheets;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<FormSheetProperty> getFormsSheetsProperties() {
        return formsSheetsProperties;
    }

    public void setFormsSheetsProperties(List<FormSheetProperty> formsSheetsProperties) {
        this.formsSheetsProperties = formsSheetsProperties;
    }

    @Override
    public String toString() {
        return
                "FormsUpdate{" +
                        "forms = '" + forms + '\'' +
                        ",sheets = '" + sheets + '\'' +
                        ",forms_sheets = '" + formsSheets + '\'' +
                        ",properties = '" + properties + '\'' +
                        ",forms_sheets_properties = '" + formsSheetsProperties + '\'' +
                        "}";
    }
}