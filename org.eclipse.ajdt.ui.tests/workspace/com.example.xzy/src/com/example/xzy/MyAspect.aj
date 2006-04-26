package com.example.xzy;

import org.eclipse.swt.widgets.Text;

public aspect MyAspect {
	after(Text t, String txt) : call(public void Text.setText(..))
			&& within(View) && target(t) && args(txt) {
		t.setText("AspectJ: "+txt);
	}
}
