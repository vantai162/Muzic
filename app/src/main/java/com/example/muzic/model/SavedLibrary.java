package com.example.muzic.model;

import java.util.List;

public class SavedLibrary {
    private List<Library> lists;

    public SavedLibrary() {}

    public SavedLibrary(List<Library> libraries)
    {
        lists = libraries;
    }

    public List<Library> getLists ()
    {
        return lists;
    }

    public void setLists (List<Library> list)
    {
        lists = list;
    }
}
