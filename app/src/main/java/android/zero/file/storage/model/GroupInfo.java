package android.zero.file.storage.model;

import java.util.List;

import android.zero.file.storage.fragment.RootsFragment.Item;

/**
 * Created by HaKr on 07/08/16.
 */

public class GroupInfo {
    public String label;
    public List<Item> itemList;

    public GroupInfo(String text, List<Item> list){
        label = text;
        itemList = list;
    }
}
