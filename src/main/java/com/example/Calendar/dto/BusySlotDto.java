package com.example.Calendar.dto;

public class BusySlotDto {
    private String start;            // ISO-строка c зоной
    private String end;              // ISO-строка c зоной
    private String title;            // что покажем (например «Занято»)
    private String backgroundColor;  // цвет
    private String display;

    public BusySlotDto(String start, String end, String title, String backgroundColor, String display) {
        this.start = start;
        this.end = end;
        this.title = title;
        this.backgroundColor = backgroundColor;
        this.display = display;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getTitle() {
        return title;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getDisplay() {
        return display;
    }
}
