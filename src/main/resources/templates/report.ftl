<html>
<body>
<div id="report">
    Endtb Exports
    <section class="report-section">
        <table>
            <tr>
                <td>Date</td>
                <td>Status</td>
                <td>Messages</td>
                <td>Download</td>
            </tr>
            <#list input as jobResult><tr>
                    <td>${jobResult.dateOfExecution}</td>
                    <td>${jobResult.status}</td>
                    <td>${(jobDetails.message)!}</td>
                    <td><a href="${jobResult.zipFileName}">Download</a></td>
            </tr>
            </#list>
        </table>
    </section>
</div>
</body>
</html>
