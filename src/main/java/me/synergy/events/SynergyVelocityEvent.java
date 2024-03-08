package me.synergy.events;

public class SynergyVelocityEvent {

    private String identifier;
    private String[] args;

    public SynergyVelocityEvent(String identifier, String[] args) {
        this.identifier = identifier;
        this.args = args;
    }

	public SynergyVelocityEvent(String identifier) {
        this.identifier = identifier;
	}

    public String getIdentifier() {
        return this.identifier;
    }
    
    public String[] getArgs() {
        return this.args;
    }

	public SynergyVelocityEvent setArguments(String[] args) {
        this.args = args;
        return this;
	}
	
	public SynergyVelocityEvent setArgument(String args) {
        this.args = new String[] {args};
        return this;
	}

}