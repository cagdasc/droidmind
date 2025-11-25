package com.cacaosd.droidmind.mind.layout.model.ios

import com.cacaosd.droidmind.mind.layout.model.ElementType

internal val iosElementMap = mapOf(
    ElementType.Alert to setOf("XCUIElementTypeAlert"),
    ElementType.App to setOf("XCUIElementTypeApplication"),
    ElementType.Button to setOf(
        "XCUIElementTypeButton",
        "XCUIElementTypeDecrementArrow",
        "XCUIElementTypeIncrementArrow",
        "XCUIElementTypeDisclosureTriangle",
        "XCUIElementTypeHandle",
        "XCUIElementTypeKey",
        "XCUIElementTypeLink",
        "XCUIElementTypeMenuButton",
        "XCUIElementTypePageIndicator",
        "XCUIElementTypePopUpButton",
        "XCUIElementTypeToolbarButton",
        "XCUIElementTypeRadioButton",
        "XCUIElementTypeTab"
    ),
    ElementType.Cell to setOf("XCUIElementTypeCell"),
    ElementType.CheckBox to setOf("XCUIElementTypeCheckBox"),
    ElementType.Column to setOf("XCUIElementTypeTableColumn"),
    ElementType.DateInput to setOf("XCUIElementTypeDatePicker"),
    ElementType.Element to setOf(
        "XCUIElementTypeOther",
        "XCUIElementTypeAny",
        "XCUIElementTypeMatte",
        "XCUIElementTypeMenuBarItem",
        "XCUIElementTypeMenuItem",
        "XCUIElementTypeRuler",
        "XCUIElementTypeRulerMarker",
        "XCUIElementTypeSplitter",
        "XCUIElementTypeStatusItem",
        "XCUIElementTypeTimeline"
    ),
    ElementType.Grid to setOf("XCUIElementTypeGrid"),
    ElementType.Icon to setOf("XCUIElementTypeIcon", "XCUIElementTypeDockItem"),
    ElementType.Image to setOf("XCUIElementTypeImage"),
    ElementType.Indicator to setOf(
        "XCUIElementTypeLevelIndicator",
        "XCUIElementTypeProgressIndicator",
        "XCUIElementTypeRatingIndicator",
        "XCUIElementTypeRelevanceIndicator",
        "XCUIElementTypeValueIndicator"
    ),
    ElementType.Input to setOf("XCUIElementTypeColorWell"),
    ElementType.List to setOf("XCUIElementTypeCollectionView"),
    ElementType.Map to setOf("XCUIElementTypeMap"),
    ElementType.Menu to setOf("XCUIElementTypeMenu", "XCUIElementTypeMenuBar"),
    ElementType.Modal to setOf(
        "XCUIElementTypeDrawer",
        "XCUIElementTypeDialog",
        "XCUIElementTypePopover"
    ),
    ElementType.Nav to setOf("XCUIElementTypeNavigationBar"),
    ElementType.PickerInput to setOf("XCUIElementTypePickerWheel"),
    ElementType.RadioInput to setOf("XCUIElementTypeRadioGroup"),
    ElementType.Row to setOf(
        "XCUIElementTypeTableRow",
        "XCUIElementTypeOutlineRow",
        "XCUIElementTypeSegmentedControl",
        "XCUIElementTypeTouchBar"
    ),
    ElementType.Scrollable to setOf("XCUIElementTypeScrollView"),
    ElementType.SearchInput to setOf("XCUIElementTypeSearchField"),
    ElementType.SliderInput to setOf(
        "XCUIElementTypeSlider",
        "XCUIElementTypeStepper",
        "XCUIElementTypeScrollBar"
    ),
    ElementType.Spinner to setOf("XCUIElementTypeActivityIndicator"),
    ElementType.SwitchInput to setOf("XCUIElementTypeSwitch"),
    ElementType.Table to setOf("XCUIElementTypeTable"),
    ElementType.Text to setOf(
        "XCUIElementTypeStaticText",
        "XCUIElementTypeTextView",
        "XCUIElementTypeHelpTag"
    ),
    ElementType.TextInput to setOf(
        "XCUIElementTypeTextField",
        "XCUIElementTypeSecureTextField",
        "XCUIElementTypeComboBox"
    ),
    ElementType.ToggleInput to setOf("XCUIElementTypeToggle"),
    ElementType.Toolbar to setOf("XCUIElementTypeToolbar"),
    ElementType.UI to setOf("AppiumAUT"),
    ElementType.View to setOf(
        "XCUIElementTypeBrowser",
        "XCUIElementTypeGroup",
        "XCUIElementTypeKeyboard",
        "XCUIElementTypeLayoutArea",
        "XCUIElementTypeLayoutItem",
        "XCUIElementTypeOutline",
        "XCUIElementTypePicker",
        "XCUIElementTypeSheet",
        "XCUIElementTypeSplitGroup",
        "XCUIElementTypeStatusBar",
        "XCUIElementTypeTabBar",
        "XCUIElementTypeTabGroup"
    ),
    ElementType.WebView to setOf("XCUIElementTypeWebView"),
    ElementType.Window to setOf("XCUIElementTypeWindow")
)
