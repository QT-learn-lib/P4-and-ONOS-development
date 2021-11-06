/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.nextworking.ipv4;

import com.google.common.collect.Lists;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onlab.packet.MacAddress;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.slf4j.LoggerFactory.getLogger;

// by qt
import org.onosproject.nextworking.ipv4.common.Utils;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupService;
import java.util.HashSet;

/**
 * IPv4 application which provides forwarding between each pair of hosts via
 * IPv4 protocol as defined in IPv4.p4.
 * <p>
 * The app works by listening for host events. Each time a new host is
 * discovered, it provisions a tunnel between that host and all the others.
 */
@Component(immediate = true)
public class IPv4App {

    private static final String APP_NAME = "org.onosproject.nextworking.IPv4";

    private static final int DEFAULT_BROADCAST_GROUP_ID = 255;

    // Default priority used for flow rules installed by this app.
    private static final int FLOW_RULE_PRIORITY = 100;

    //    private final HostListener hostListener = new InternalHostListener();
    private ApplicationId appId;

    private static final Logger log = getLogger(IPv4App.class);

    //--------------------------------------------------------------------------
    // ONOS core services needed by this application.
    //--------------------------------------------------------------------------

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private GroupService groupService;

//    @Reference(cardinality = ReferenceCardinality.MANDATORY)
//    private TopologyService topologyService;
//
//    @Reference(cardinality = ReferenceCardinality.MANDATORY)
//    private HostService hostService;

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    @Activate
    public void activate() {
        // Register app and event listeners.
        log.info("Starting...");
        appId = coreService.registerApplication(APP_NAME);

        // Host discovery
//        hostService.addListener(hostListener);

        /* Distributed L2 flow able */

        // S1
        insertL2ForwardRule(DeviceId.deviceId("device:bmv2:s1"), MacAddress.valueOf("00:00:00:00:00:01"),
                PortNumber.fromString("1"));
        insertL2ForwardRule(DeviceId.deviceId("device:bmv2:s1"), MacAddress.valueOf("00:00:00:00:00:02"),
                PortNumber.fromString("2"));
        // S2
        insertL2ForwardRule(DeviceId.deviceId("device:bmv2:s2"), MacAddress.valueOf("00:00:00:00:00:01"),
                PortNumber.fromString("1"));
        insertL2ForwardRule(DeviceId.deviceId("device:bmv2:s2"), MacAddress.valueOf("00:00:00:00:00:02"),
                PortNumber.fromString("2"));
        // S3
        insertL2ForwardRule(DeviceId.deviceId("device:bmv2:s3"), MacAddress.valueOf("00:00:00:00:00:01"),
                PortNumber.fromString("1"));
        insertL2ForwardRule(DeviceId.deviceId("device:bmv2:s3"), MacAddress.valueOf("00:00:00:00:00:02"),
                PortNumber.fromString("2"));

        /* Distributed L3 flow able */

//        // S1
//        insertipv4ForwardRule(DeviceId.deviceId("device:bmv2:s1"), Ip4Address.valueOf("10.0.0.1"),
//                PortNumber.fromString("1"), MacAddress.valueOf("00:00:00:00:00:01"));
//        insertipv4ForwardRule(DeviceId.deviceId("device:bmv2:s1"), Ip4Address.valueOf("10.0.0.2"),
//                PortNumber.fromString("2"), MacAddress.valueOf("08:00:00:00:00:01"));
//        // S2
//        insertipv4ForwardRule(DeviceId.deviceId("device:bmv2:s2"), Ip4Address.valueOf("10.0.0.1"),
//                PortNumber.fromString("1"), MacAddress.valueOf("08:00:00:00:00:01"));
//        insertipv4ForwardRule(DeviceId.deviceId("device:bmv2:s2"), Ip4Address.valueOf("10.0.0.2"),
//                PortNumber.fromString("2"), MacAddress.valueOf("08:00:00:00:00:02"));
//        // S3
//        insertipv4ForwardRule(DeviceId.deviceId("device:bmv2:s3"), Ip4Address.valueOf("10.0.0.1"),
//                PortNumber.fromString("1"), MacAddress.valueOf("08:00:00:00:00:02"));
//        insertipv4ForwardRule(DeviceId.deviceId("device:bmv2:s3"), Ip4Address.valueOf("10.0.0.2"),
//                PortNumber.fromString("2"), MacAddress.valueOf("00:00:00:00:00:02"));

        /* Setup multicast tables for switches */

//        setupMtcastTables(DeviceId.deviceId("device:bmv2:s1"));

    }

    @Deactivate
    public void deactivate() {
        // Remove listeners and clean-up flow rules.
        log.info("Stopping...");
//        hostService.removeListener(hostListener);
        flowRuleService.removeFlowRulesById(appId);
        log.info("STOPPED");
    }

    /**
     * Returns the application ID.
     * 个人添加方法
     *
     * @return application ID
     */
    ApplicationId getAppId() {
        return appId;
    }

    /**
     * Sets up multicast tables for switches
     *
     * @param switchId the switch to set up
     *
     */
    private void setupMtcastTables(DeviceId switchId){
        // 1: Insert multicast group;
        insertMulticastGroup(switchId);
        // 2: Insert multicast table.
        insertMulticastFlowRules(switchId);
    }


    /**
     * Inserts an ALL group in the ONOS core to replicate packets on all host
     * facing ports. This group will be used to broadcast all ARP/NDP requests.
     * <p>
     * ALL groups in ONOS are equivalent to P4Runtime packet replication engine
     * (PRE) Multicast groups.
     *
     * @param switchId the device where to install the group
     */
    private void insertMulticastGroup(DeviceId switchId) {

        // Replicate packets where we know hosts are attached.
        Set<PortNumber> ports = new HashSet<PortNumber>();
        ports.add(PortNumber.fromString("1"));
        ports.add(PortNumber.fromString("2"));
        ports.add(PortNumber.fromString("3"));

        if (ports.isEmpty()) {
            // Stop here.
            log.warn("Device {} has 0 host facing ports", switchId);
            return;
        }

        log.info("Adding L2 multicast group with {} ports on {}...",
                ports.size(), switchId);

        // Forge group object.
        final GroupDescription multicastGroup = Utils.buildMulticastGroup(
                appId, switchId, DEFAULT_BROADCAST_GROUP_ID, ports);

        // Insert.
        groupService.addGroup(multicastGroup);
    }

    /**
     * Insert flow rules matching ethernet destination
     * broadcast/multicast addresses (e.g. ARP requests, NDP Neighbor
     * Solicitation, etc.). Such packets should be processed by the multicast
     * group created before.
     * <p>
     * This method will be called at component activation for each device
     * (switch) known by ONOS, and every time a new device-added event is
     * captured by the InternalDeviceListener defined below.
     *
     * @param switchId device ID where to install the rules
     */
    private void insertMulticastFlowRules(DeviceId switchId) {

        // Table name
        PiTableId l2MticastTableId = PiTableId.of("MyIngress.t_l2_multicast");

        // Exact match on dstMAC
        final PiMatchFieldId dstMACMatchFieldId = PiMatchFieldId.of("hdr.ethernet.dstAddr");
        // Match ARP request - Match exactly FF:FF:FF:FF:FF:FF
        final PiCriterion match = PiCriterion.builder()
                .matchExact(
                        dstMACMatchFieldId,
                        MacAddress.valueOf("FF:FF:FF:FF:FF:FF").toBytes())
                .build();

        // Parameters of action table.
        final PiActionParamId portParamId = PiActionParamId.of("gid");
        final PiActionParam portParam = new PiActionParam(portParamId, DEFAULT_BROADCAST_GROUP_ID);

        // Action table
        final PiActionId set_multicast_group = PiActionId.of("MyIngress.set_multicast_group");
        final PiAction action = PiAction.builder()
                .withId(set_multicast_group)
                .withParameter(portParam)
                .build();

        log.info("Inserting L2 multicast rule on switch {}: table={}, match={}, action={}",
                switchId, l2MticastTableId, match, action);

        insertPiFlowRule(switchId, l2MticastTableId, match, action);
    }

    /**
     * Generates and insert a flow rule to perform the L2 Forwarding
     * function for the given switchId, dstIpAddr, outPort and dstMAC.
     *
     * @param switchId  Switch ID
     * @param ethdstAddr  Destination MAC
     * @param outPort  Output port where to forward packets
     */
    private void insertL2ForwardRule(DeviceId switchId,
                                     MacAddress dstMAC,
                                     PortNumber outPort) {

        // Table name
        PiTableId l2ForwardTableId = PiTableId.of("MyIngress.t_l2_fwd");

        // Ternary match on dstMAC
        final PiMatchFieldId dstMACMatchFieldId = PiMatchFieldId.of("hdr.ethernet.dstAddr");
        final PiCriterion match = PiCriterion.builder()
                .matchTernary(
                        dstMACMatchFieldId,
                        dstMAC.toBytes(),
                        MacAddress.valueOf("FF:FF:FF:FF:FF:FF").toBytes())
                .build();

        // Parameters of action table.
        final PiActionParamId portParamId = PiActionParamId.of("port");
        final PiActionParam portParam = new PiActionParam(portParamId, (short) outPort.toLong());

        // Action table
        final PiActionId set_out_port = PiActionId.of("MyIngress.set_out_port");
        final PiAction action = PiAction.builder()
                .withId(set_out_port)
                .withParameter(portParam)
                .build();

        log.info("Inserting L2 fwd rule on switch {}: table={}, match={}, action={}",
                switchId, l2ForwardTableId, match, action);

        insertPiFlowRule(switchId, l2ForwardTableId, match, action);
    }

    /**
     * Generates and insert a flow rule to perform the L3 Forwarding
     * function for the given switchId, dstIpAddr, outPort and dstMAC.
     *
     * @param switchId   Switch ID
     * @param dstIpAddr  Destination IP address
     * @param outPort    Output port where to forward packets
     * @param ethdstAddr Destination MAC
     */
    private void insertipv4ForwardRule(DeviceId switchId,
                                       Ip4Address dstIpAddr,
                                       PortNumber outPort,
                                       MacAddress ethdstAddr) {

        // Table name
        PiTableId ipv4ForwardTableId = PiTableId.of("MyIngress.ipv4_lpm");

        // Lpm match on dstIpAddr
        final PiMatchFieldId ipDestMatchFieldId = PiMatchFieldId.of("hdr.ipv4.dstAddr");
        final PiCriterion match = PiCriterion.builder()
                .matchLpm(ipDestMatchFieldId, dstIpAddr.toOctets(), 32)
                .build();

        // Parameters of action table.
        final PiActionParamId portParamId = PiActionParamId.of("port");
        final PiActionParam portParam = new PiActionParam(portParamId, (short) outPort.toLong());
        final PiActionParamId ethdst_ParamId = PiActionParamId.of("dstAddr");
        final PiActionParam ethdstParam = new PiActionParam(ethdst_ParamId, ethdstAddr.toBytes());

        // Action table
        final PiActionId ipv4_forward = PiActionId.of("MyIngress.ipv4_forward");
        final PiAction action = PiAction.builder()
                .withId(ipv4_forward)
                .withParameter(ethdstParam)
                .withParameter(portParam)
                .build();

        log.info("Inserting L3 fwd rule on switch {}: table={}, match={}, action={}",
                switchId, ipv4ForwardTableId, match, action);

        insertPiFlowRule(switchId, ipv4ForwardTableId, match, action);
    }

    /**
     * Inserts a flow rule in the system that using a PI criterion and action.
     *
     * @param switchId    switch ID
     * @param tableId     table ID
     * @param piCriterion PI criterion
     * @param piAction    PI action
     */
    private void insertPiFlowRule(DeviceId switchId, PiTableId tableId,
                                  PiCriterion piCriterion, PiAction piAction) {
        FlowRule rule = DefaultFlowRule.builder()
                .forDevice(switchId)
                .forTable(tableId)
                .fromApp(appId)
                .withPriority(FLOW_RULE_PRIORITY)
                .makePermanent()
                .withSelector(DefaultTrafficSelector.builder()
                        .matchPi(piCriterion).build())
                .withTreatment(DefaultTrafficTreatment.builder()
                        .piTableAction(piAction).build())
                .build();
        flowRuleService.applyFlowRules(rule);
    }
}