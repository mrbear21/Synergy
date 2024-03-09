package me.synergy.events;

public class SynergyVelocityEvent {
  private String identifier;
  
  private String player;
  
  private boolean waitForPlayer = false;
  
  private String[] args;
  
  public SynergyVelocityEvent(String identifier) {
    this.identifier = identifier;
  }
  
  public SynergyVelocityEvent(String identifier, String player, String[] args) {
    this.identifier = identifier;
    this.player = player;
    this.args = args;
  }
  
  public SynergyVelocityEvent(String identifier, String player, String waitForPlayer, String[] args2) {
    this.identifier = identifier;
    this.player = player;
    this.waitForPlayer = Boolean.valueOf(waitForPlayer).booleanValue();
  }
  
  public String getIdentifier() {
    return this.identifier;
  }
  
  public String[] getArgs() {
    (new String[1])[0] = "N/A";
    return (this.args != null) ? this.args : new String[1];
  }
  
  public String getPlayer() {
    return this.player;
  }
  
  public boolean getWaitForPlayerIfOffline() {
    return this.waitForPlayer;
  }
  
  public SynergyVelocityEvent setArguments(String[] args) {
    this.args = args;
    return this;
  }
  
  public SynergyVelocityEvent setArgument(String arg) {
    this.args = new String[] { arg };
    return this;
  }
  
  public SynergyVelocityEvent setPlayer(String player) {
    this.player = player;
    return this;
  }
  
  public SynergyVelocityEvent setWaitForPlayerIfOffline(boolean waitForPlayer) {
    this.waitForPlayer = waitForPlayer;
    return this;
  }
}
