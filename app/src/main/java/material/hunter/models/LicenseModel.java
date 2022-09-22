package material.hunter.models;

public class LicenseModel {

    private String title;
    private String license;

    public LicenseModel(String title, String license) {
        this.title = title;
        this.license = license;
    }

    public String getTitle() {
        return title;
    }

    public String getLicense() {
        return license;
    }
}