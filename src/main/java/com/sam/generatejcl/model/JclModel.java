package com.sam.generatejcl.model;

public class JclModel {
    private String actualfile;
    private String lrecl;
    private String recfm;
    private String dsorg;

    @Override
    public String toString() {
        return "JclModel{" +
                "actualfile='" + actualfile + '\'' +
                ", lrecl='" + lrecl + '\'' +
                ", recfm='" + recfm + '\'' +
                ", dsorg='" + dsorg + '\'' +
                '}';
    }

    public String getDsorg() {
        return dsorg;
    }

    public void setDsorg(String dsorg) {
        this.dsorg = dsorg;
    }

    public JclModel(String actualfile, String lrecl, String recfm, String dsorg) {
        this.actualfile = actualfile;
        this.lrecl = lrecl;
        this.recfm = recfm;
        this.dsorg = dsorg;
    }

    public JclModel() {
    }

    public JclModel(String actualfile, String lrecl,String recfm) {
        this.actualfile = actualfile;
        this.lrecl = lrecl;
        this.recfm = recfm;
    }

    public String getActualfile() {
        return actualfile;
    }

    public void setActualfile(String actualfile) {
        this.actualfile = actualfile;
    }

    public String getLrecl() {
        return lrecl;
    }

    public void setLrecl(String lrecl) {
        this.lrecl = lrecl;
    }

    public String getRecfm() {
        return recfm;
    }

    public void setRecfm(String recfm) {
        this.recfm = recfm;
    }

}
