package com.github.taiem92dn

import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.WizardTemplateProvider
import com.github.taiem92dn.templatepluginsetup.mviSetupTemplate

class WizardTemplateProviderImpl  : WizardTemplateProvider() {

    override fun getTemplates(): List<Template> = listOf(mviSetupTemplate)
}