<%--
 ~ Password Management Servlets (PWM)
 ~ http://www.pwm-project.org
 ~
 ~ Copyright (c) 2006-2009 Novell, Inc.
 ~ Copyright (c) 2009-2021 The PWM Project
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>
<%--
       THIS FILE IS NOT INTENDED FOR END USER MODIFICATION.
       See the README.TXT file in WEB-INF/jsp before making changes.
--%>


<%@ page import="password.pwm.http.tag.conditional.PwmIfTest" %>

<%@ taglib uri="pwm" prefix="pwm" %>
<link href="<pwm:url url='/public/resources/adminStyle.css' addContext="true"/>" rel="stylesheet" type="text/css" media="screen"/>

<div class="admin-breadcrumb-navigation-button">
    <a href="<pwm:url addContext="true" url="/private"/>">
        <button type="submit" class="navbutton">
            <pwm:if test="<%=PwmIfTest.showIcons%>"><span class="btn-icon pwm-icon pwm-icon-arrow-up"></span></pwm:if>
            <pwm:display key="Title_MainPage" bundle="Display"/>
        </button>
    </a>
</div>
<div class="admin-breadcrumb-navigation-button">
    <a href="<pwm:url addContext="true" url="/private/admin"/>">
        <button type="submit" class="navbutton">
            <pwm:if test="<%=PwmIfTest.showIcons%>"><span class="btn-icon pwm-icon pwm-icon-arrow-up"></span></pwm:if>
            <pwm:display key="Title_Admin" bundle="Display"/>
        </button>
    </a>
</div>

