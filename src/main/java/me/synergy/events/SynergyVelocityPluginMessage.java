package me.synergy.events;

public class SynergyVelocityPluginMessage {

    private String identifier;
    private String[] args;

    public SynergyVelocityPluginMessage(String identifier, String[] args) {
        this.identifier = identifier;
        this.args = args;
    }

	public SynergyVelocityPluginMessage(String identifier) {
        this.identifier = identifier;
	}

    public String getIdentifier() {
        return this.identifier;
    }
    
    public String[] getArgs() {
        return this.args;
    }

	public SynergyVelocityPluginMessage setArguments(String[] args) {
        this.args = args;
        return this;
	}
	
	public SynergyVelocityPluginMessage setArgument(String args) {
        this.args = new String[] {args};
        return this;
	}

}