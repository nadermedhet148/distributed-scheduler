package io.worker.constant;

public final class Globals {
  private Globals() {}

  public record ConvFlowState() {
    public static final int START = 0;
    public static final int RESUME_FLOW = 1;
    public static final int AWAIT_VISITOR = 2;
    public static final int AWAIT_CALL = 3;
    public static final int AGENT_HANDOVER = 4;
    public static final int AGENT_HANDOVER_RESOLVE = 5;
    public static final int AGENT_TAKEOVER = 6;
    public static final int AGENT_TAKEOVER_RESOLVE = 7;
    public static final int UNDEFINED = 8;
    public static final int CLOSED = 10;
    public static final int REJECTED = 11;
  }

  public record ConvEntity() {
    public static final int ENTITY_VISITOR_ID = 1;
    public static final int ENTITY_BOT_ID = 2;
    public static final int ENTITY_AGENT_ID = 3;
    public static final int ENTITY_SCHEDULER_ID = 4;
    public static final int ENTITY_SYSTEM_ID = 5;

    public static final String ENTITY_SCHEDULER = "SCHEDULER";
    public static final String ENTITY_SYSTEM = "SYSTEM";
    public static final String ENTITY_AGENT = "AGENT";
  }

  public record AgentAutomationRule() {
    public static final String AUTO_RESOLVE_UNRESPONSIVE_CUSTOMER = "auto-resolve-unresponsive-customer";
    public static final String NUDGE_CUSTOMER = "nudge-customer";
    public static final String ENGAGE_CUSTOMER_UNASSIGNED_CONVERSATION = "engage-unassigned-conversation";
    public static final String UNRESPONSIVE_AGENT_NEW_CONVERSATION = "unresponsive-new-conversation";
    public static final String UNRESPONSIVE_AGENT_ONGOING_CONVERSATION = "unresponsive-ongoing-conversation";
    public static final String GREET_CUSTOMER = "greet-message";
    public static final String FAREWELL_CUSTOMER = "farewell-customer";

  }

  public record SystemInternalAutomationRule() {
    public static final String CSAT_AUTO_RESOLVE = "csat-auto-resolve";
    public static final String AUTO_ASSIGN_UNASSIGNED_CHAT = "auto_assign_unassigned_chat";
  }

  public record AutomationEventSourceType() {
    public static final int CONVERSATION_MESSAGE = 1;
    public static final int AGENT_NOTIFICATION = 2;
    public static final int TEAM_ASSIGNMENT = 3;
  }

  public record AutomationTaskCategories() {
    public static final int CSAT_MESSAGE = 1;
    public static final int AGENT_CONSOLE_MESSAGE = 2;
    public static final int AUTO_ASSIGN_UNASSIGNED_CHATS = 3;
  }

}
