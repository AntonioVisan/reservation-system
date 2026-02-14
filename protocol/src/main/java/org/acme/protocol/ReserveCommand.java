package org.acme.protocol;

public class ReserveCommand implements Command{
    private Long slotId;

    public ReserveCommand(Long slotId) {
        this.slotId = slotId;
    }

    public Long getSlotId(){
        return slotId;
    }


}
