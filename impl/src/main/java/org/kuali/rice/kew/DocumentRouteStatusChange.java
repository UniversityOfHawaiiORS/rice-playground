package org.kuali.rice.kew;

/**
 * <p>
 * <Title>
 * </p>
 * <p>
 * <Description>
 * </p>
 * <p>
 * <p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * <p>
 * Company: UIS - Indiana University
 * </p>
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class DocumentRouteStatusChange implements IDocumentEvent {

	private static final long serialVersionUID = -5170568498563302803L;
	private String appDocId;
	private Long routeHeaderId;
	private String newRouteStatus;
	private String oldRouteStatus;

	public DocumentRouteStatusChange(Long routeHeaderId, String appDocId, String oldStatus, String newStatus) {
		this.routeHeaderId = routeHeaderId;
		this.appDocId = appDocId;
		this.newRouteStatus = newStatus;
		this.oldRouteStatus = oldStatus;
	}

	public String getDocumentEventCode() {
		return ROUTE_STATUS_CHANGE;
	}

	public Long getRouteHeaderId() {
		return routeHeaderId;
	}

	public String getNewRouteStatus() {
		return newRouteStatus;
	}

	public String getOldRouteStatus() {
		return oldRouteStatus;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RouteHeaderID ").append(routeHeaderId);
		buffer.append(" changing from routeStatus ").append(oldRouteStatus);
		buffer.append(" to routeStatus ").append(newRouteStatus);

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.kuali.rice.kew.IDocumentEvent#getAppDocId()
	 */
	public String getAppDocId() {
		return this.appDocId;
	}
}
