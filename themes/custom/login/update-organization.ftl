<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        Данные организации
    <#elseif section = "form">
        <form id="kc-sms-code-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="organization_type" class="${properties.kcLabelClass!}">Организационно-правовая
                        форма</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <select name="organization_type">
                        <option value="IP">Индивидуальный предприниматель</option>
                        <option value="OOO">Общество с ограниченной ответственностью</option>
                    </select>
                </div>
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="inn" class="${properties.kcLabelClass!}">ИНН</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="inn" name="inn" class="${properties.kcInputClass!}" autofocus/>
                </div>
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="mobile_number" class="${properties.kcLabelClass!}">Номер телефона для связи
                        менеджера</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="mobile_number" name="mobile_number" class="${properties.kcInputClass!}"
                           autofocus/>
                </div>
            </div>
            <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" value="Подтвердить"/>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
