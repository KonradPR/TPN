package com.example.tpn;

import android.widget.LinearLayout;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class CustomSwipeAdapterTest {

    @Test
    public void testGetCount(){
        CustomSwipeAdapter a = new CustomSwipeAdapter(RuntimeEnvironment.application);
        assertEquals(5,a.getCount());
    }

    @Test
    public void testIsViewFromObject(){
        CustomSwipeAdapter a = new CustomSwipeAdapter(RuntimeEnvironment.application);
        LinearLayout o = new LinearLayout(RuntimeEnvironment.application);
        assertTrue(a.isViewFromObject(o,o));
    }
}
