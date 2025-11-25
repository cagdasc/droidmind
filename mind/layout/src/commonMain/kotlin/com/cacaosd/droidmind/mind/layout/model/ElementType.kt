package com.cacaosd.droidmind.mind.layout.model

import kotlinx.serialization.Serializable

@Serializable
enum class ElementType {
    Alert,
    App,
    Button,
    Cell,
    CheckBox,
    Column,
    DateInput,
    Element,
    Grid,
    Icon,
    Image,
    Indicator,
    Input,
    List,
    Map,
    Menu,
    Modal,
    Nav,
    PickerInput,
    RadioInput,
    Row,
    Scrollable,
    SearchInput,
    SliderInput,
    Spinner,
    SwitchInput,
    Table,
    Text,
    TextInput,
    ToggleInput,
    Toolbar,
    UI,
    Video,
    View,
    WebView,
    Window,
    Unknown;


    fun canHaveText(): Boolean =
        when (this) {
            // Always visible text
            Button,
            Cell,
            CheckBox,
            DateInput,
            Input,
            PickerInput,
            RadioInput,
            SearchInput,
            SliderInput,
            SwitchInput,
            Text,
            TextInput,
            ToggleInput,
            Toolbar,
            Menu,
            List -> true

            // Sometimes have visible text depending on dump content
            Alert,
            Modal,
            Window,
            Nav,
            Table,
            Row,
            Column,
            View,
            Element -> true

            // Usually not visible text
            App,
            Grid,
            Icon,
            Image,
            Indicator,
            Map,
            Scrollable,
            Spinner,
            UI,
            Video,
            WebView,
            Unknown -> false
        }

    companion object {

        val INTERACTIVE = setOf(
            Button,
            CheckBox,
            RadioInput,
            SwitchInput,
            ToggleInput,
            SliderInput,
            DateInput,
            Input,
            TextInput,
            SearchInput,
            PickerInput,
            Spinner
        )

        val TEXTUAL = setOf(
            Text,
            Button,
            Cell,
            CheckBox,
            RadioInput,
            Input,
            SearchInput,
            TextInput,
            ToggleInput,
            Toolbar,
            Menu,
            List
        )

        val CONTAINERS = setOf(
            Column,
            Row,
            Grid,
            View,
            Element,
            Scrollable,
            Table,
            List,
            Nav
        )

        val MEDIA = setOf(
            Image,
            Video,
            Icon
        )

        val WINDOWS = setOf(
            Alert,
            Modal,
            Window,
            App
        )

        val WEB = setOf(
            WebView
        )
    }
}
