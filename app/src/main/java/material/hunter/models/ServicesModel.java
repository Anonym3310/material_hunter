package material.hunter.models;

public class ServicesModel {

    private String ServiceName;
    private String CommandforStartService;
    private String CommandforStopService;
    private String CommandforCheckServiceStatus;
    private String RunOnChrootStart;
    private String Status;

    public ServicesModel(
            String ServiceName,
            String CommandforStartService,
            String CommandforStopService,
            String CommandforCheckServiceStatus,
            String RunOnChrootStart,
            String Status) {
        this.ServiceName = ServiceName;
        this.CommandforStartService = CommandforStartService;
        this.CommandforStopService = CommandforStopService;
        this.CommandforCheckServiceStatus = CommandforCheckServiceStatus;
        this.RunOnChrootStart = RunOnChrootStart;
        this.Status = Status;
    }

    public String getServiceName() {
        return ServiceName;
    }

    public void setServiceName(String ServiceName) {
        this.ServiceName = ServiceName;
    }

    public String getCommandforStartService() {
        return CommandforStartService;
    }

    public void setCommandforStartService(String CommandforStartService) {
        this.CommandforStartService = CommandforStartService;
    }

    public String getCommandforStopService() {
        return CommandforStopService;
    }

    public void setCommandforStopService(String CommandforStopService) {
        this.CommandforStopService = CommandforStopService;
    }

    public String getCommandforCheckServiceStatus() {
        return CommandforCheckServiceStatus;
    }

    public void setCommandforCheckServiceStatus(String CommandforCheckServiceStatus) {
        this.CommandforCheckServiceStatus = CommandforCheckServiceStatus;
    }

    public String getRunOnChrootStart() {
        return RunOnChrootStart;
    }

    public void setRunOnChrootStart(String RunOnChrootStart) {
        this.RunOnChrootStart = RunOnChrootStart;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }
}