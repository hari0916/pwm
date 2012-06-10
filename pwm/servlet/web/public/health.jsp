<%@ page import="password.pwm.util.stats.Statistic" %>
<%--
~ Password Management Servlets (PWM)
~ http://code.google.com/p/pwm/
~
~ Copyright (c) 2006-2009 Novell, Inc.
~ Copyright (c) 2009-2012 The PWM Project
~
~ This program is free software; you can redistribute it and/or modify
~ it under the terms of the GNU General Public License as published by
~ the Free Software Foundation; either version 2 of the License, or
~ (at your option) any later version.
~
~ This program is distributed in the hope that it will be useful,
~ but WITHOUT ANY WARRANTY; without even the implied warranty of
~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
~ GNU General Public License for more details.
~
~ You should have received a copy of the GNU General Public License
~ along with this program; if not, write to the Free Software
~ Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
--%>

<!DOCTYPE html>
<%@ page language="java" session="true" isThreadSafe="true"
         contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="pwm" prefix="pwm" %>
<html dir="<pwm:LocaleOrientation/>">
<%@ include file="/WEB-INF/jsp/fragment/header.jsp" %>
<% try { password.pwm.PwmSession.getPwmSession(session).unauthenticateUser(); } catch (Exception e) { }%>
<body class="tundra">
<jsp:include page="/WEB-INF/jsp/fragment/header-body.jsp">
    <jsp:param name="pwm.PageName" value="PWM Health"/>
</jsp:include>
<div id="wrapper">
    <div id="centerbody">
        <div id style="z-index: 3; position: relative; background: white; opacity: 0.9">
            <table id="form">
                <tr>
                    <td class="title" colspan="10">
                        <pwm:Display key="Title_Application"/> Health
                    </td>
                </tr>
                <tr>
                    <td colspan="10"  style="margin:0; padding:0">
                        <div id="healthBody" style="border:0; margin:0; padding:0"></div>
                    </td>
                </tr>
                <tr>
                    <td class="title" colspan="10">
                        Password Changes
                    </td>
                </tr>
                <tr>
                    <td colspan="10"  style="margin:0; padding:0">
                        <div id="statsChart" style="height: 150px; z-index: 3; margin: 0; padding: 0">
                        </div>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <div id="floatparent">
    </div>
    <script type="text/javascript">
        var MAX_NODES = 5 * 1000;
        var splatCount = 0;

        PWM_GLOBAL['pwm-health'] = 'GOOD';

        function displayRandomFloat(text) {
            require(["dojo","dojo/window"],function(dojo){
                var floatParent = getObject("floatparent");
                var vs = dojo.window.getBox();

                var topPos = Math.floor((Math.random() * vs.h));
                var leftPos = Math.floor((Math.random() * vs.w));
                var bottomPos = vs.h - topPos;
                var rightPos = vs.w - leftPos;

                var styleText = "position: absolute; ";
                if (topPos > (vs.h / 2)) {
                    styleText = styleText + "bottom: " + bottomPos + "px; ";
                } else {
                    styleText = styleText + "top: " + topPos + "px; ";
                }
                if (leftPos > (vs.w / 2)) {
                    styleText = styleText + "right: " + rightPos +"px; ";
                } else {
                    styleText = styleText + "left: " + leftPos +"px; ";
                }


                styleText = styleText + "padding: 4px; z-index:2; border-radius: 5px;";
                var divId = "randomPwDiv" + splatCount % MAX_NODES;

                { // remove old node
                    var existingDiv = getObject(divId);
                    if (existingDiv != null) {
                        floatParent.removeChild(existingDiv);
                    }
                }

                var div = document.createElement('div');
                div.innerHTML = text;
                div.id = divId;
                div.setAttribute("class",'health-' + PWM_GLOBAL['pwm-health']);

                div.setAttribute("style",styleText);
                floatParent.appendChild(div);
            });
        }

        function fetchRandomPassword() {
            require(["dojo"],function(dojo){
                dojo.xhrPost({
                    url: PWM_GLOBAL['url-restservice'] + "/randompassword" + "?pwmFormID=" + PWM_GLOBAL['pwmFormID'],
                    headers: {"Accept":"application/json"},
                    dataType: "json",
                    timeout: 15000,
                    sync: false,
                    preventCache: true,
                    handleAs: "json",
                    load:  function(resultInfo) {
                        var password = resultInfo["password"];
                        displayRandomFloat(password);
                        doNext();
                    },
                    error: function(errorObj){
                        var password = "server unreachable";
                        displayRandomFloat(password);
                        doNext();
                    }
                });
            });
        }

        function doNext() {
            splatCount++;
            var randomInterval = Math.floor(Math.random() * 15 * 1000);
            setTimeout(function(){
                fetchRandomPassword();
            },randomInterval);
        }

        require(["dojo","dojo/ready"],function(){
            showPwmHealth('healthBody', false);
            setTimeout(function(){ doNext(); },60 * 1000);
            setInterval(function(){
                showStatChart('<%=Statistic.PASSWORD_CHANGES%>',30,'statsChart');
            },60 * 1000);
            showStatChart('<%=Statistic.PASSWORD_CHANGES%>',30,'statsChart');
        });
    </script>
</div>
</body>
</html>
