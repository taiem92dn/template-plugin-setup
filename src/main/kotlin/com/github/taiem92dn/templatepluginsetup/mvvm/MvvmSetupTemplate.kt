package com.github.taiem92dn.templatepluginsetup.mvvm

import com.android.tools.idea.wizard.template.*
import com.android.tools.idea.wizard.template.impl.defaultPackageNameParameter

private const val MIN_SDK = 16

val mvvmSetupTemplate
    get() = template {
//        revision = 2
        name = "MVVM Setup"
        description = "Add many files use for MVVM template"
        minApi = MIN_SDK
//        minBuildApi = MIN_SDK
        category = Category.Other
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.ActivityGallery, WizardUiContext.MenuEntry,
            WizardUiContext.NewProject, WizardUiContext.NewModule)

        val packageNameParam = defaultPackageNameParameter

        val entityName = stringParameter {
            name = "Test Name"
            default = "Mvvm"
            help = "This is a test name"
            constraints = listOf(Constraint.NONEMPTY)
        }

        val addNetwork = booleanParameter {
            name = "Add network"
            default = true
            help = "If true, add network package and related class to base package"
        }

        val overWriteBuildGradle = booleanParameter {
            name = "Overwrite build gradle files"
            default = true
            help = "If true, build gradle files would be overwritten if not some dependencies and " +
                    "plugins would be added"
        }

        widgets(
            TextFieldWidget(entityName),
            PackageNameWidget(basePackageName),
            CheckBoxWidget(addNetwork),
            CheckBoxWidget(overWriteBuildGradle)
        )

        recipe = { data: TemplateData ->
            mvvmTemplateSetup(
                data as ModuleTemplateData,
                basePackageName.value,
                addNetwork.value,
                overWriteBuildGradle.value,
                entityName.value,
            )
        }
    }

val basePackageName = stringParameter {
    name = "App base package name"
    visible = { !isNewModule }
    default = "com.mycompany.myapp"
    constraints = listOf(Constraint.PACKAGE)
    suggest = { packageName }
}
