package com.github.taiem92dn

import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.WizardTemplateProvider
import com.github.taiem92dn.templatepluginsetup.activity.createActivity
import com.github.taiem92dn.templatepluginsetup.listapi.listApiTemplate
import com.github.taiem92dn.templatepluginsetup.mvvm.mvvmSetupTemplate
import com.github.taiem92dn.templatepluginsetup.paging.pagingTemplate
import com.github.taiem92dn.templatepluginsetup.pagingdb.pagingDatabaseTemplate

class WizardTemplateProviderImpl  : WizardTemplateProvider() {

    override fun getTemplates(): List<Template> =
        listOf(
            createActivity,
            mvvmSetupTemplate,
            listApiTemplate,
            pagingTemplate,
            pagingDatabaseTemplate
        )
}