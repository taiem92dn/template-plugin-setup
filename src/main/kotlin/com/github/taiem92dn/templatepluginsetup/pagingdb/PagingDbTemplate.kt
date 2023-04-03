package com.github.taiem92dn.templatepluginsetup.pagingdb

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.CheckBoxWidget
import com.android.tools.idea.wizard.template.Constraint
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.PackageNameWidget
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.booleanParameter
import com.android.tools.idea.wizard.template.stringParameter
import com.android.tools.idea.wizard.template.template

private const val MIN_SDK = 16

val pagingDatabaseTemplate
    get() = template {
//        revision = 2
        name = "List Api Fragment with Paging And Database"
        description = "Add many files use for Paging + Database Fragment"
        minApi = MIN_SDK
//        minBuildApi = MIN_SDK
        category = Category.Fragment
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.ActivityGallery, WizardUiContext.MenuEntry,)

        val itemName = stringParameter {
            name = "Model Item Name"
            default = "Model"
            help = "This is a model item name"
            constraints = listOf(Constraint.NONEMPTY)
        }

        val addCoordinator = booleanParameter {
            name = "Add CoordinatorLayout"
            default = true
            help = "If true, add CorrdinatorLayout in fragment layout"
        }

        val addBackButton = booleanParameter {
            name = "Add Back Button into Fragment View"
            default = false
            help = "If true, Add Back Button into Fragment View"
        }

        val addRepository = booleanParameter {
            name = "Add Repository, DataSource files"
            default = true
            help = "If true, Add Repository & DataSource files"
        }

        val addContentApiXml = booleanParameter {
            name = "Add Content Api Xml"
            default = true
            help = "If true, Add Content Api Xml"
        }

        val addRetrofitService = booleanParameter {
            name = "Add Retrofit Service file"
            default = true
            help = "If true, Add Retrofit Service file and also input an Api path"
        }

        val addListResponseFile = booleanParameter {
            name = "Add List Response file"
            default = true
            help = "If true, Add List Response file"
        }

        val addPagingCommonFile = booleanParameter {
            name = "Add Paging Common file (list_state_item.xml, ItemLoadStateAdapter, ItemLoadStateViewHolder)"
            default = true
            help = "If true, Add Paging Common file (list_state_item.xml, ItemLoadStateAdapter, ItemLoadStateViewHolder)"
        }

        val apiPath = stringParameter {
            name = "Api Path"
            default = ""
            help = "This is Api Path that use in the function of retrofit service"
            constraints = listOf(Constraint.NONEMPTY)
        }

        val addDatabaseFile = booleanParameter {
            name = "Add Database file"
            default = true
            help = "If true, add Database file, pls also add database name if this checkbox is checked"
        }

        val databaseName = stringParameter {
            name = "Database name"
            default = "App name"
            help = "This is Database, it can be absent if you don't need to add database file"
            constraints = listOf()
        }

        widgets(
            PackageNameWidget(basePackageName),
            TextFieldWidget(itemName),
            CheckBoxWidget(addCoordinator),
            CheckBoxWidget(addBackButton),
            CheckBoxWidget(addRepository),
            CheckBoxWidget(addContentApiXml),
            CheckBoxWidget(addRetrofitService),
            CheckBoxWidget(addListResponseFile),
            CheckBoxWidget(addPagingCommonFile),
            TextFieldWidget(apiPath),
            CheckBoxWidget(addDatabaseFile),
            TextFieldWidget(databaseName),
        )

        recipe = { data: TemplateData ->
            pagingDatabaseRecipe(
                data as ModuleTemplateData,
                basePackageName.value,
                itemName.value,
                addCoordinator.value,
                addBackButton.value,
                addRepository.value,
                addContentApiXml.value,
                addRetrofitService.value,
                addListResponseFile.value,
                addPagingCommonFile.value,
                apiPath.value,
                addDatabaseFile.value,
                databaseName.value
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
