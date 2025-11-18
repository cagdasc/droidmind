package com.cacaosd.droidmind.mind.layout.model

val iosElementMapping: Map<String, Element> = mapOf(

    // --- High-level containers / structural ---
    "XCUIElementTypeApplication" to Element.Container,
    "XCUIElementTypeWindow" to Element.Container,
    "XCUIElementTypeOther" to Element.Container,
    "XCUIElementTypeCollectionView" to Element.Container,
    "XCUIElementTypeTable" to Element.Container,
    "XCUIElementTypeCell" to Element.Container,
    "XCUIElementTypeScrollView" to Element.Container,
    "XCUIElementTypePageIndicator" to Element.Container,
    "XCUIElementTypeToolbar" to Element.Container,
    "XCUIElementTypeTabBar" to Element.Container,
    "XCUIElementTypeNavigationBar" to Element.Container,
    "XCUIElementTypeStatusBar" to Element.Container,
    "XCUIElementTypeSplitGroup" to Element.Container,
    "XCUIElementTypeLayoutArea" to Element.Container,
    "XCUIElementTypeLayoutItem" to Element.Container,
    "XCUIElementTypePopover" to Element.Container,
    "XCUIElementTypeSheet" to Element.Container,
    "XCUIElementTypeBrowser" to Element.Container,
    "XCUIElementTypeWebView" to Element.Container,
    "XCUIElementTypeCollectionView" to Element.Container,
    "XCUIElementTypeList" to Element.Container,
    "XCUIElementTypeTableRow" to Element.Container,
    "XCUIElementTypeTableColumn" to Element.Container,
    "XCUIElementTypeOutline" to Element.Container,
    "XCUIElementTypeDisclosureTriangle" to Element.Container,

    // --- Buttons / interaction ---
    "XCUIElementTypeButton" to Element.TextBased.Button,
    "XCUIElementTypeLink" to Element.TextBased.Button,
    "XCUIElementTypeSwitch" to Element.TextBased.Button,
    "XCUIElementTypeToggle" to Element.TextBased.Button,
    "XCUIElementTypeRadioButton" to Element.TextBased.Button,
    "XCUIElementTypeCheckBox" to Element.TextBased.Button,
    "XCUIElementTypeStepper" to Element.TextBased.Button,
    "XCUIElementTypeSlider" to Element.TextBased.Button,
    "XCUIElementTypePickerWheel" to Element.TextBased.Button,
    "XCUIElementTypePicker" to Element.TextBased.Button,
    "XCUIElementTypeMenuItem" to Element.TextBased.Button,
    "XCUIElementTypeMenuButton" to Element.TextBased.Button,
    "XCUIElementTypeBrowser" to Element.TextBased.Button,
    "XCUIElementTypePopover" to Element.TextBased.Button,

    // --- Inputs / text entry ---
    "XCUIElementTypeTextField" to Element.TextBased.InputField,
    "XCUIElementTypeSecureTextField" to Element.TextBased.InputField,
    "XCUIElementTypeSearchField" to Element.TextBased.InputField,
    "XCUIElementTypeTextView" to Element.TextBased.InputField,
    "XCUIElementTypeKeyboard" to Element.TextBased.InputField, // keyboard is interactive input

    // --- Labels / static text ---
    "XCUIElementTypeStaticText" to Element.TextBased.Label,
    "XCUIElementTypeInlineTextCompletion" to Element.TextBased.Label,
    "XCUIElementTypeIcon" to Element.TextBased.Label,
    "XCUIElementTypeImage" to Element.TextBased.Label,
    "XCUIElementTypeProgressIndicator" to Element.TextBased.Label,
    "XCUIElementTypeActivityIndicator" to Element.TextBased.Label,
    "XCUIElementTypeAlert" to Element.TextBased.Label,
    "XCUIElementTypeDialog" to Element.TextBased.Label,
    "XCUIElementTypeTextArea" to Element.TextBased.Label,
    "XCUIElementTypeGrid" to Element.TextBased.Label,
    "XCUIElementTypeStaticGroup" to Element.TextBased.Label,

    // --- Rare or system-specific types (usually non-interactive) ---
    "XCUIElementTypeMap" to Element.Container,
    "XCUIElementTypeDisclosureTriangle" to Element.Container,
    "XCUIElementTypeValueIndicator" to Element.TextBased.Label,
    "XCUIElementTypeHandle" to Element.TextBased.Button,
    "XCUIElementTypeRelevanceIndicator" to Element.TextBased.Label,
    "XCUIElementTypeRubberBandIndicator" to Element.TextBased.Label,
    "XCUIElementTypeColorWell" to Element.TextBased.Button,
    "XCUIElementTypeSegmentedControl" to Element.Container,
    "XCUIElementTypeSwitch" to Element.TextBased.Button,
    "XCUIElementTypeToggle" to Element.TextBased.Button,

    // --- Fallback ---
    "UNKNOWN" to Element.Unknown
)
