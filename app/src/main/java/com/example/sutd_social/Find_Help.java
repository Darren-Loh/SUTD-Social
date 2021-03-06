package com.example.sutd_social;

public class Find_Help {
    private String name;
    private String pillar;
    private String profilePicture;
    private int confidence_lvl;
    private String skills;

    public Find_Help(String name, String pillar) {
        this.name = name;
        this.pillar = pillar;
    }

    public Find_Help(String name, String pillar, int confidence_lvl, String skills) {
        this.name = name;
        this.pillar = pillar;
        this.confidence_lvl = confidence_lvl;
        this.skills = skills;
    }

    public Find_Help(String name, String pillar, int confidence_lvl, String profilePicture, String skills) {
        this.name = name;
        this.pillar = pillar;
        this.confidence_lvl = confidence_lvl;
        this.profilePicture = profilePicture;
        this.skills = skills;
    }

    public Find_Help(String name, String pillar, String skills) {
        this.name = name;
        this.pillar = pillar;
        this.skills = skills;
    }

    public Find_Help(String name, String pillar, int confidence_lvl) {
        this.name = name;
        this.pillar = pillar;
        this.confidence_lvl = confidence_lvl;
    }

    public Find_Help(String name, String pillar, String profilePicture, String skills) {
        this.name = name;
        this.pillar = pillar;
        this.profilePicture = profilePicture;
        this.skills = skills;
    }

    public int getConfidence_lvl() {
        return confidence_lvl;
    }

    public void setConfidence_lvl(int confidence_lvl) {
        this.confidence_lvl = confidence_lvl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPillar() {
        return pillar;
    }

    public void setPillar(String pillar) {
        this.pillar = pillar;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
