package com.bss.arrahmanlyrics.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mohan on 6/18/17.
 */
public class AlwaysMarqueeTextView extends android.support.v7.widget.AppCompatTextView {
    protected boolean a;

    public AlwaysMarqueeTextView(Context context) {
        super(context);
        a = false;
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        a = false;
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attributeset, int i) {
        super(context, attributeset, i);
        a = false;
    }

    public boolean isFocused() {
        return a || super.isFocused();
    }

    public void setAlwaysMarquee(boolean flag) {
        setSelected(flag);
        setSingleLine(flag);
        if (flag)
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
        a = flag;
    }
}
