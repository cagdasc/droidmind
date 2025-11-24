package com.cacaosd.droidmind.mind.layout.model.android

import com.cacaosd.droidmind.mind.layout.model.ElementType

internal val androidElement = mapOf(
    ElementType.Alert to setOf("android.widget.Toast"),
    ElementType.Button to setOf(
        "android.widget.Button",
        "android.widget.ImageButton",
        "android.widget.RadioButton",
        "android.widget.QuickContactBadge"
    ),
    ElementType.CheckBox to setOf("android.widget.CheckBox"),
    ElementType.DateInput to setOf("android.widget.DatePicker"),
    ElementType.Element to setOf(
        "android.widget.Space",
        "android.widget.TwoLineListItem"
    ),
    ElementType.Grid to setOf("android.widget.GridLayout", "android.widget.GridView"),
    ElementType.Image to setOf("android.widget.ImageView"),
    ElementType.Indicator to setOf("android.widget.RatingBar", "android.widget.ProgressBar"),
    ElementType.List to setOf(
        "android.widget.ListView",
        "android.widget.ExpandableListView",
        "android.widget.Gallery"
    ),
    ElementType.Menu to setOf(
        "android.widget.ActionMenuView",
        "android.widget.PopupMenu"
    ),
    ElementType.Modal to setOf(
        "android.widget.ListPopupWindow",
        "android.widget.PopupWindow",
        "android.widget.SlidingDrawer",
        "android.widget.Magnifier"
    ),
    ElementType.PickerInput to setOf(
        "android.widget.NumberPicker",
        "android.widget.TimePicker",
        "android.widget.CalendarView"
    ),
    ElementType.RadioInput to setOf("android.widget.RadioGroup"),
    ElementType.Row to setOf("android.widget.TableRow"),
    ElementType.Scrollable to setOf(
        "android.widget.ScrollView",
        "android.widget.HorizontalScrollView"
    ),
    ElementType.SearchInput to setOf("android.widget.SearchView"),
    ElementType.SliderInput to setOf("android.widget.SeekBar"),
    ElementType.Spinner to setOf("android.widget.Spinner"),
    ElementType.SwitchInput to setOf("android.widget.Switch"),
    ElementType.Table to setOf("android.widget.TableLayout"),
    ElementType.Text to setOf(
        "android.widget.TextView",
        "android.widget.Chronometer",
        "android.widget.TextClock"
    ),
    ElementType.TextInput to setOf(
        "android.widget.EditText",
        "android.widget.AutoCompleteTextView",
        "android.widget.MultiAutoCompleteTextView"
    ),
    ElementType.ToggleInput to setOf(
        "android.widget.CheckedTextView",
        "android.widget.ToggleButton"
    ),
    ElementType.Toolbar to setOf("android.widget.Toolbar"),
    ElementType.UI to setOf("hierarchy"),
    ElementType.Video to setOf("android.widget.VideoView"),
    ElementType.View to setOf(
        "android.widget.FrameLayout",
        "android.widget.LinearLayout",
        "android.widget.RelativeLayout",
        "android.view.View",
        "android.view.ViewGroup",
        "android.widget.MediaController",
        "android.widget.StackView"
    )
)
