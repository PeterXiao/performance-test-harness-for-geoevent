/*
 Copyright 1995-2015 Esri

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
package com.esri.geoevent.test.performance.ui;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import jfxtras.labs.scene.control.BigDecimalField;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.esri.geoevent.test.performance.Protocol;
import com.esri.geoevent.test.performance.jaxb.ConsumerConfig;
import com.esri.geoevent.test.performance.jaxb.Fixture;
import com.esri.geoevent.test.performance.jaxb.ProducerConfig;
import com.esri.geoevent.test.performance.jaxb.Property;
import com.esri.geoevent.test.performance.jaxb.RampTest;
import com.esri.geoevent.test.performance.jaxb.RemoteHost;
import com.esri.geoevent.test.performance.jaxb.Simulation;
import com.esri.geoevent.test.performance.jaxb.StressTest;
import com.esri.geoevent.test.performance.jaxb.Test;
import com.esri.geoevent.test.performance.jaxb.TestType;
import com.esri.geoevent.test.performance.jaxb.TimeTest;
import com.esri.geoevent.test.performance.ui.controls.RestrictiveTextField;

public class FixtureController implements Initializable {

    //UI Elements

    @FXML
    public Label nameLabel;
    @FXML
    public TextField nameField;
    @FXML
    public Button editNameBtn;

    // producers
    @FXML
    public Label producersLabel;
    @FXML
    public TableView<ConnectableRemoteHost> producersTable;
    @FXML
    public TableColumn<ConnectableRemoteHost, String> producersNameColumn;
    @FXML
    public TableColumn<ConnectableRemoteHost, Boolean> producersDeleteColumn;
    @FXML
    public TextField producerHostName;
    @FXML
    public RestrictiveTextField producerPort;
    @FXML
    public Button producerAddBtn;
    @FXML
    public Label producersProtocolLabel;
    @FXML
    public ComboBox<Protocol> producersProtocolType;
    @FXML
    public Label producersPropsLabel;
    @FXML
    public TableView<Property> producersPropsTable;
    @FXML
    public TableColumn<Property, String> producersPropNameColumn;
    @FXML
    public TableColumn<Property, String> producersPropValueColumn;

    // consumers
    @FXML
    public Label consumersLabel;
    @FXML
    public TableView<ConnectableRemoteHost> consumersTable;
    @FXML
    public TableColumn<ConnectableRemoteHost, String> consumersNameColumn;
    @FXML
    public TableColumn<ConnectableRemoteHost, Boolean> consumersDeleteColumn;
    @FXML
    public TextField consumerHostName;
    @FXML
    public RestrictiveTextField consumerPort;
    @FXML
    public Button consumerAddBtn;
    @FXML
    public Label consumersProtocolLabel;
    @FXML
    public ComboBox<Protocol> consumersProtocolType;
    @FXML
    public Label consumersPropsLabel;
    @FXML
    public TableView<Property> consumersPropsTable;
    @FXML
    public TableColumn<Property, String> consumersPropNameColumn;
    @FXML
    public TableColumn<Property, String> consumersPropValueColumn;

    // simulation
    @FXML
    public VBox simulationBox;
    @FXML
    public Label testTypeLabel;
    @FXML
    public ComboBox<TestType> testType;

    // time test
    @FXML
    public GridPane timeTestGroup;
    @FXML
    public Label eventsPerSecLabel;
    @FXML
    public BigDecimalField eventsPerSec;
    @FXML
    public Label totalTimeInSecLabel;
    @FXML
    public BigDecimalField totalTimeInSec;
    @FXML
    public Label expectedResultCountPerSecLabel;
    @FXML
    public BigDecimalField expectedResultCountPerSec;
    @FXML
    public Label staggeringIntervalLabel;
    @FXML
    public BigDecimalField staggeringInterval;

    //stress test
    @FXML
    public GridPane stressTestGroup;
    @FXML
    public Label numOfEventsLabel;
    @FXML
    public BigDecimalField numOfEvents;
    @FXML
    public Label iterationsLabel;
    @FXML
    public BigDecimalField iterations;
    @FXML
    public Label expectedResultCountLabel;
    @FXML
    public BigDecimalField expectedResultCount;

    // ramp test
    @FXML
    public GridPane rampTestGroup;
    @FXML
    public Label minEventsLabel;
    @FXML
    public BigDecimalField minEvents;
    @FXML
    public Label maxEventsLabel;
    @FXML
    public BigDecimalField maxEvents;
    @FXML
    public Label eventsToAddPerTestLabel;
    @FXML
    public BigDecimalField eventsToAddPerTest;
    @FXML
    public Label expectedResultCountPerTestLabel;
    @FXML
    public BigDecimalField expectedResultCountPerTest;

    @FXML
    public Button applySimulationBtn;

    // member vars
    private Fixture fixture;
    private boolean isDefault;
    private boolean isEditingName;
    private Map<Protocol, ObservableList<Property>> producerPropertiesCache;
    private Map<Protocol, ObservableList<Property>> consumerPropertiesCache;

	// statics
    //private static final Callback<TableColumn<ConnectableRemoteHost,Boolean>,TableCell<ConnectableRemoteHost,Boolean>> CONNECTABLE_CELL_FACTORY = (p) -> new ConnectedTableCell<>();
    private static final String SAVE_IMAGE_SOURCE = "images/save.png";
    private static final String EDIT_IMAGE_SOURCE = "images/pencil.png";

    /**
     * Sets the isDefault flag.
     *
     * @param isDefault flag
     */
    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
        applyIsDefault(isDefault);
    }

    /**
     * Sets the Fixture for this controller.
     *
     * @param fixture of type {@link Fixture}
     */
    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
        applyFixture(fixture);
    }

    /**
     * retrieves the fixture - populating it as needed
     *
     * @return Fixture 
     */
    public Fixture getFixture() {
        return this.fixture;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initPropertiesCache();

        nameLabel.setText(UIMessages.getMessage("UI_FIXTURE_NAME_LABEL"));
        nameField.setText(fixture != null ? fixture.getName() : "");
        editNameBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_FIXTURE_EDIT_NAME_DESC")));

		//set up the tables
        //producers
        producersLabel.setText(UIMessages.getMessage("UI_PRODUCERS_LABEL"));
        producersNameColumn.setText(UIMessages.getMessage("UI_PRODUCERS_NAME_COL_LABEL"));
        producersNameColumn.prefWidthProperty().bind(producersTable.widthProperty().multiply(0.83));
        producersDeleteColumn.prefWidthProperty().bind(producersTable.widthProperty().multiply(0.16));
        producersDeleteColumn.setCellFactory(callback -> new DeleteableTableCell<ConnectableRemoteHost>());
        producerHostName.setPromptText(UIMessages.getMessage("UI_PRODUCER_HOST_NAME_DESC"));
        producerPort.setPromptText(UIMessages.getMessage("UI_PRODUCER_PORT_DESC"));
        producerAddBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_PRODUCER_ADD_BTN_DESC")));
        producersProtocolLabel.setText(UIMessages.getMessage("UI_PROTOCOL_LABEL"));
        producersProtocolType.setItems(getProducerProtocolList());
        producersProtocolType.setValue(Protocol.TCP);
        producersPropsLabel.setText(UIMessages.getMessage("UI_PROPERTY_LABEL"));
        producersPropNameColumn.setText(UIMessages.getMessage("UI_PROPERTY_NAME_COL_LABEL"));
        producersPropNameColumn.prefWidthProperty().bind(producersPropsTable.widthProperty().multiply(0.48));
        producersPropValueColumn.setText(UIMessages.getMessage("UI_PROPERTY_VALUE_COL_LABEL"));
        producersPropValueColumn.prefWidthProperty().bind(producersPropsTable.widthProperty().multiply(0.5));
        producersPropValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        producersPropValueColumn.setOnEditCommit(event -> {
            Property property = (Property) event.getTableView().getItems().get(event.getTablePosition().getRow());
            property.setValue(event.getNewValue());
        }
        );

        //consumers
        consumersLabel.setText(UIMessages.getMessage("UI_CONSUMERS_LABEL"));
        consumersNameColumn.setText(UIMessages.getMessage("UI_CONSUMERS_NAME_COL_LABEL"));
        consumersNameColumn.prefWidthProperty().bind(consumersTable.widthProperty().multiply(0.83));
        consumersDeleteColumn.prefWidthProperty().bind(consumersTable.widthProperty().multiply(0.16));
        consumersDeleteColumn.setCellFactory(callback -> new DeleteableTableCell<ConnectableRemoteHost>());
        consumerHostName.setPromptText(UIMessages.getMessage("UI_CONSUMER_HOST_NAME_DESC"));
        consumerPort.setPromptText(UIMessages.getMessage("UI_CONSUMER_PORT_DESC"));
        consumerAddBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_CONSUMER_ADD_BTN_DESC")));
        consumersProtocolLabel.setText(UIMessages.getMessage("UI_PROTOCOL_LABEL"));
        consumersProtocolType.setItems(getConsumerProtocolList());
        consumersProtocolType.setValue(Protocol.TCP);
        consumersPropsLabel.setText(UIMessages.getMessage("UI_PROPERTY_LABEL"));
        consumersPropNameColumn.setText(UIMessages.getMessage("UI_PROPERTY_NAME_COL_LABEL"));
        consumersPropNameColumn.prefWidthProperty().bind(consumersPropsTable.widthProperty().multiply(0.48));
        consumersPropValueColumn.setText(UIMessages.getMessage("UI_PROPERTY_VALUE_COL_LABEL"));
        consumersPropValueColumn.prefWidthProperty().bind(consumersPropsTable.widthProperty().multiply(0.5));
        consumersPropValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        consumersPropValueColumn.setOnEditCommit(event -> {
            Property property = (Property) event.getTableView().getItems().get(event.getTablePosition().getRow());
            property.setValue(event.getNewValue());
        }
        );

        // simulation
        testTypeLabel.setText(UIMessages.getMessage("UI_TEST_TYPE_LABEL"));
        testType.setTooltip(new Tooltip(UIMessages.getMessage("UI_TEST_TYPE_DESC")));
        testType.setItems(getTestTypes());
        testType.setValue(TestType.TIME);
        // time
        eventsPerSecLabel.setText(UIMessages.getMessage("UI_EVENTS_PER_SEC_LABEL"));
        totalTimeInSecLabel.setText(UIMessages.getMessage("UI_TOTAL_TIME_IN_SEC_LABEL"));
        expectedResultCountPerSecLabel.setText(UIMessages.getMessage("UI_EXPECTED_EVENTS_PER_SEC_LABEL"));
        staggeringIntervalLabel.setText(UIMessages.getMessage("UI_STAGGERING_INTERVAL_LABEL"));
        // stress
        numOfEventsLabel.setText(UIMessages.getMessage("UI_NUM_OF_EVENTS_LABEL"));
        iterationsLabel.setText(UIMessages.getMessage("UI_ITERATIONS_LABEL"));
        expectedResultCountLabel.setText(UIMessages.getMessage("UI_EXPECTED_EVENT_COUNT_LABEL"));
        // ramp
        minEventsLabel.setText(UIMessages.getMessage("UI_MIN_EVENTS_LABEL"));
        maxEventsLabel.setText(UIMessages.getMessage("UI_MAX_EVENTS_LABEL"));
        eventsToAddPerTestLabel.setText(UIMessages.getMessage("UI_EVENTS_TO_ADD_LABEL"));
        expectedResultCountPerTestLabel.setText(UIMessages.getMessage("UI_EXPECTED_EVENTS_PER_TEST_LABEL"));

        applySimulationBtn.setText(UIMessages.getMessage("UI_APPLY_SIMULATION_LABEL"));

        //set up state
        toggleEditName(null);
        setEditNameState(isEditingName);
        toggleProducerProtocolType(null);
        toggleConsumerProtocolType(null);
    }

    @FXML
    public void onNameFieldKeyPressed(final KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            setEditNameState(!isEditingName);
        }
    }

    @FXML
    public void toggleEditName(final ActionEvent event) {
        setEditNameState(!isEditingName);
    }

    private void setEditNameState(boolean newState) {
        if (newState != isEditingName) {
            isEditingName = newState;
            if (isEditingName) {
                editNameBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_FIXTURE_SAVE_NAME_DESC")));
                editNameBtn.setGraphic(new ImageView(new Image(FixtureController.class.getResourceAsStream(SAVE_IMAGE_SOURCE))));
                nameField.setEditable(true);
            } else {
                editNameBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_FIXTURE_EDIT_NAME_DESC")));
                editNameBtn.setGraphic(new ImageView(new Image(FixtureController.class.getResourceAsStream(EDIT_IMAGE_SOURCE))));
                nameField.setEditable(false);
            }
        }
    }

    @FXML
    public void onAddProducerKeyPressed(final KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            ActionEvent actionEvent = null;
            addProducer(actionEvent);
        }
    }

    @FXML
    public void addProducer(final ActionEvent event) {
        String hostName = producerHostName.getText();
        int port = NumberUtils.toInt(producerPort.getText(), 0);
        if (StringUtils.isEmpty(hostName) || port == 0) {
            //TODO: Validation error
        } else {
            ConnectableRemoteHost remoteHost = new ConnectableRemoteHost(hostName, port);
            producersTable.getItems().add(remoteHost);
            addProducer(remoteHost);
        }
    }

    @FXML
    public void onAddConsumerKeyPressed(final KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            ActionEvent actionEvent = null;
            addConsumer(actionEvent);
        }
    }

    @FXML
    public void addConsumer(final ActionEvent event) {
        String hostName = consumerHostName.getText();
        int port = NumberUtils.toInt(consumerPort.getText(), 0);
        if (StringUtils.isEmpty(hostName) || port == 0) {
            //TODO: Validation error
        } else {
            ConnectableRemoteHost remoteHost = new ConnectableRemoteHost(hostName, port);
            consumersTable.getItems().add(remoteHost);
            addConsumer(remoteHost);
        }
    }

    @FXML
    public void toggleProducerProtocolType(final ActionEvent event) {
        Protocol protocol = producersProtocolType.getValue();
        ObservableList<Property> props = producerPropertiesCache.get(protocol);
        producersPropsTable.setItems(props);
        setProducerProtocol(protocol);
    }

    @FXML
    public void toggleConsumerProtocolType(final ActionEvent event) {
        Protocol protocol = consumersProtocolType.getValue();
        ObservableList<Property> props = consumerPropertiesCache.get(protocol);
        consumersPropsTable.setItems(props);
        setConsumerProtocol(protocol);
    }

    @FXML
    public void toggleTestType(final ActionEvent event) {
        rampTestGroup.setVisible(false);
        stressTestGroup.setVisible(false);
        timeTestGroup.setVisible(false);

        TestType selectedTestType = testType.getValue();
        switch (selectedTestType) {
            case RAMP:
                rampTestGroup.setVisible(true);
                break;
            case STRESS:
                stressTestGroup.setVisible(true);
                break;
            case TIME:
            default:
                timeTestGroup.setVisible(true);
                break;
        }
    }

    @FXML
    public void applySimulation(final ActionEvent event) {
        TestType type = testType.getValue();
        if (!isSimulationValid(type)) {
            //TODO: Display validation message
            return;
        }

        switch (type) {
            case RAMP:
                RampTest rampTest = new RampTest();
                rampTest.setEventsToAddPerTest(eventsToAddPerTest.getNumber().intValue());
                rampTest.setMaxEvents(maxEvents.getNumber().intValue());
                rampTest.setMinEvents(minEvents.getNumber().intValue());
                if (expectedResultCountPerTest.getNumber() != null && expectedResultCountPerTest.getNumber().intValue() > 0) {
                    rampTest.setExpectedResultCountPerTest(expectedResultCountPerTest.getNumber().intValue());
                }
                setTest(rampTest);
                break;

            case STRESS:
                StressTest stressTest = new StressTest();
                if (expectedResultCount.getNumber() != null && expectedResultCount.getNumber().intValue() > 0) {
                    stressTest.setExpectedResultCount(expectedResultCount.getNumber().intValue());
                }
                stressTest.setIterations(iterations.getNumber().intValue());
                stressTest.setNumOfEvents(numOfEvents.getNumber().intValue());
                setTest(stressTest);
                break;

            case TIME:
                TimeTest timeTest = new TimeTest();
                if (expectedResultCountPerSec.getNumber() != null && expectedResultCountPerSec.getNumber().intValue() > 0) {
                    timeTest.setExpectedResultCountPerSec(expectedResultCountPerSec.getNumber().intValue());
                }
                if (staggeringInterval.getNumber() != null && staggeringInterval.getNumber().intValue() > 0) {
                    timeTest.setExpectedResultCountPerSec(staggeringInterval.getNumber().intValue());
                }
                timeTest.setEventsPerSec(eventsPerSec.getNumber().intValue());
                timeTest.setTotalTimeInSec(totalTimeInSec.getNumber().intValue());
                setTest(timeTest);
                break;

            default:
                break;
        }
    }

    /**
     * Validates the simulation inputs
     *
     * @param type
     */
    private boolean isSimulationValid(TestType type) {
        switch (testType.getValue()) {
            case RAMP:
                if (eventsToAddPerTest.getNumber().intValue() <= 0) {
                    return false;
                }
                if (maxEvents.getNumber().intValue() <= 0) {
                    return false;
                }
                if (minEvents.getNumber().intValue() <= 0) {
                    return false;
                }
                return true;

            case STRESS:
                if (numOfEvents.getNumber().intValue() <= 0) {
                    return false;
                }
                if (iterations.getNumber().intValue() <= 0) {
                    return false;
                }
                return true;

            case TIME:
                if (eventsPerSec.getNumber().intValue() <= 0) {
                    return false;
                }
                if (totalTimeInSec.getNumber().intValue() <= 0) {
                    return false;
                }
                return true;

            default:
                break;
        }
        return false;
    }

    private ObservableList<TestType> getTestTypes() {
        ArrayList<TestType> list = new ArrayList<TestType>();
        list.add(TestType.TIME);
        list.add(TestType.STRESS);
        list.add(TestType.RAMP);
        return FXCollections.observableList(list);
    }

    /**
     * Helper method used to set the Test on the Fixture object
     *
     * @param test
     */
    private void setTest(Test test) {
        if (fixture == null) {
            fixture = new Fixture();
        }
        if (fixture.getSimulation() == null) {
            fixture.setSimulation(new Simulation());
        }
        fixture.getSimulation().setTest(test);
    }

    /**
     * Helper method used to set the Protocol on the Producer Config object
     *
     * @param protocol {@link Protocol}
     */
    private void setProducerProtocol(Protocol protocol) {
        if (fixture == null) {
            fixture = new Fixture();
        }
        if (fixture.getProducerConfig() == null) {
            fixture.setProducerConfig(new ProducerConfig());
        }
        fixture.getProducerConfig().setProtocol(protocol);
    }

    /**
     * Helper method used to set the Protocol on the Consumer Config object
     *
     * @param protocol {@link Protocol}
     */
    private void setConsumerProtocol(Protocol protocol) {
        if (fixture == null) {
            fixture = new Fixture();
        }
        if (fixture.getConsumerConfig() == null) {
            fixture.setConsumerConfig(new ConsumerConfig());
        }
        fixture.getConsumerConfig().setProtocol(protocol);
    }

    /**
     * Helper method used to add producers to the Producer Config object
     *
     * @param remoteHost {@link ConnectableRemoteHost}
     */
    private void addProducer(ConnectableRemoteHost remoteHost) {
        if (fixture == null) {
            fixture = new Fixture();
        }
        if (fixture.getProducerConfig() == null) {
            fixture.setProducerConfig(new ProducerConfig());
        }
        fixture.getProducerConfig().getProducers().add(new RemoteHost(remoteHost.getHost(), remoteHost.getCommandPort()));
    }

    /**
     * Helper method used to add producers to the Consumer Config object
     *
     * @param remoteHost {@link ConnectableRemoteHost}
     */
    private void addConsumer(ConnectableRemoteHost remoteHost) {
        if (fixture == null) {
            fixture = new Fixture();
        }
        if (fixture.getConsumerConfig() == null) {
            fixture.setConsumerConfig(new ConsumerConfig());
        }
        fixture.getConsumerConfig().getConsumers().add(new RemoteHost(remoteHost.getHost(), remoteHost.getCommandPort()));
    }

    /**
     * Helper methods to delete producer from the model
     *
     * @param remoteHost {@link ConnectableRemoteHost}
     */
    private void deleteProducer(ConnectableRemoteHost remoteHost) {
        if (fixture == null || fixture.getProducerConfig() == null || fixture.getProducerConfig().getProducers() == null) {
            return;
        }

        fixture.getProducerConfig().getProducers().remove(new RemoteHost(remoteHost.getHost(), remoteHost.getCommandPort()));
    }

    /**
     * Helper methods to delete consumer from the model
     *
     * @param remoteHost {@link ConnectableRemoteHost}
     */
    private void deleteConsumer(ConnectableRemoteHost remoteHost) {
        if (fixture == null || fixture.getConsumerConfig() == null || fixture.getConsumerConfig().getConsumers() == null) {
            return;
        }

        fixture.getConsumerConfig().getConsumers().remove(new RemoteHost(remoteHost.getHost(), remoteHost.getCommandPort()));
    }

    /**
     * Helper method to toggle if we are editing the default fixxture or not.
     *
     * @param isDefault boolean
     */
    private void applyIsDefault(boolean isDefault) {
        if (isDefault) {
            editNameBtn.setVisible(false);
            nameField.setEditable(false);
        } else {
            editNameBtn.setVisible(true);
            nameField.setEditable(true);
        }
    }

    /**
     * Helper method that is used to set the fixture object and apply it to all
     * of the ui elements
     *
     * @param fixture
     */
    private void applyFixture(Fixture fixture) {
        if (fixture == null) {
            return;
        }

        if (isDefault) {
            nameField.setEditable(false);
            editNameBtn.setVisible(false);
        } else {
            nameField.setText(fixture.getName());
            nameField.setEditable(true);
            editNameBtn.setVisible(true);
        }

        // apply if necessary
        if (fixture.getConsumerConfig() != null) {
            fixture.getConsumerConfig().apply(fixture.getDefaultConfig());
        }
        if (fixture.getProducerConfig() != null) {
            fixture.getProducerConfig().apply(fixture.getDefaultConfig());
        }

        // get the consumers
        List<RemoteHost> consumers = new ArrayList<RemoteHost>();
        if (fixture.getConsumerConfig() != null) {
            if (fixture.getConsumerConfig().getProtocol() != null && fixture.getConsumerConfig().getProtocol() != Protocol.UNKNOWN) {
                consumersProtocolType.setValue(fixture.getConsumerConfig().getProtocol());
                if (fixture.getConsumerConfig().getProperties() != null) {
                    ObservableList<Property> propsInCache = consumerPropertiesCache.get(fixture.getConsumerConfig().getProtocol());
                    propsInCache.stream().forEach(prop -> {
                        prop.setValue(fixture.getConsumerConfig().getPropertyValue(prop.getName()));
                    });
                }
            }
            if (fixture.getConsumerConfig().getConsumers() != null) {
                consumers = fixture.getConsumerConfig().getConsumers();
            } else {
                consumers.add(fixture.getConsumerConfig().getDefaultRemoteHost());
            }
        }
        List<ConnectableRemoteHost> connectableConsumers = convert(consumers);
        consumersTable.getItems().clear();
        consumersTable.getItems().addAll(connectableConsumers);

        // get the producers
        List<RemoteHost> producers = new ArrayList<RemoteHost>();
        if (fixture.getProducerConfig() != null) {
            if (fixture.getProducerConfig().getProtocol() != null && fixture.getProducerConfig().getProtocol() != Protocol.UNKNOWN) {
                producersProtocolType.setValue(fixture.getProducerConfig().getProtocol());
                if (fixture.getProducerConfig().getProperties() != null) {
                    ObservableList<Property> propsInCache = producerPropertiesCache.get(fixture.getProducerConfig().getProtocol());
                    propsInCache.stream().forEach(prop -> {
                        prop.setValue(fixture.getProducerConfig().getPropertyValue(prop.getName()));
                    });
                }
            }

            if (fixture.getProducerConfig().getProducers() != null) {
                producers = fixture.getProducerConfig().getProducers();
            } else {
                producers.add(fixture.getProducerConfig().getDefaultRemoteHost());
            }
        }
        List<ConnectableRemoteHost> connectableProducers = convert(producers);
        producersTable.getItems().clear();
        producersTable.getItems().addAll(connectableProducers);

        // set the test
        if (fixture.getSimulation() == null) {
            fixture.setSimulation(new Simulation());
        }
        if (fixture.getSimulation().getTest() == null) {
            fixture.getSimulation().setTest(new TimeTest());
        }

        Test test = fixture.getSimulation().getTest();
        testType.setValue(test.getType());
        switch (test.getType()) {
            case RAMP:
                RampTest rampTest = (RampTest) test;
                if (rampTest.getEventsToAddPerTest() > 0) {
                    eventsToAddPerTest.setNumber(new BigDecimal(rampTest.getEventsToAddPerTest()));
                }
                if (rampTest.getMaxEvents() > 0) {
                    maxEvents.setNumber(new BigDecimal(rampTest.getMaxEvents()));
                }
                if (rampTest.getMinEvents() > 0) {
                    minEvents.setNumber(new BigDecimal(rampTest.getMinEvents()));
                }
                if (rampTest.getExpectedResultCountPerTest() > 0) {
                    expectedResultCountPerTest.setNumber(new BigDecimal(rampTest.getExpectedResultCountPerTest()));
                }
                break;

            case STRESS:
                StressTest stressTest = (StressTest) test;
                if (stressTest.getNumOfEvents() > 0) {
                    numOfEvents.setNumber(new BigDecimal(stressTest.getNumOfEvents()));
                }
                if (stressTest.getIterations() > 0) {
                    iterations.setNumber(new BigDecimal(stressTest.getIterations()));
                }
                if (stressTest.getExpectedResultCount() > 0) {
                    expectedResultCount.setNumber(new BigDecimal(stressTest.getExpectedResultCount()));
                }
                break;

            case TIME:
                TimeTest timeTest = (TimeTest) test;
                if (timeTest.getEventsPerSec() > 0) {
                    eventsPerSec.setNumber(new BigDecimal(timeTest.getEventsPerSec()));
                }
                if (timeTest.getTotalTimeInSec() > 0) {
                    totalTimeInSec.setNumber(new BigDecimal(timeTest.getTotalTimeInSec()));
                }
                if (timeTest.getExpectedResultCountPerSec() > 0) {
                    expectedResultCountPerSec.setNumber(new BigDecimal(timeTest.getExpectedResultCountPerSec()));
                }
                if (timeTest.getStaggeringInterval() > 0) {
                    staggeringInterval.setNumber(new BigDecimal(timeTest.getStaggeringInterval()));
                }
                break;

            default:
                break;
        }
        toggleTestType(null);
    }

    /**
     * Convert from {@link RemoteHost} to {@link ConnectableRemoteHost}
     *
     * @param remoteHosts
     * @return Returns a list of Connectable Remote Host
     */
    private List<ConnectableRemoteHost> convert(List<RemoteHost> remoteHosts) {
        if (remoteHosts == null) {
            return null;
        }

        return remoteHosts.stream().map(host -> new ConnectableRemoteHost(host)).collect(Collectors.toList());
    }

    /**
     * TODO: More finite control of when we connect to producers and consumers
     *
     * @param remoteHost
     */
//  private void connect(final ConnectableRemoteHost remoteHost)
//  {
//  	
//  }
    /**
     * TODO: More finite control of when we connect to producers and consumers
     *
     * @param remoteHost
     */
//  private void disconnect(final ConnectableRemoteHost remoteHost)
//  {
//  	
//  }
    private ObservableList<Protocol> getProducerProtocolList() {
        ArrayList<Protocol> list = new ArrayList<Protocol>();
        list.add(Protocol.ACTIVE_MQ);
        list.add(Protocol.KAFKA);
        list.add(Protocol.RABBIT_MQ);
        list.add(Protocol.STREAM_SERVICE);
        list.add(Protocol.TCP);
        list.add(Protocol.TCP_SERVER);
        list.add(Protocol.WEBSOCKETS);
        list.add(Protocol.WEBSOCKET_SERVER);
        return FXCollections.observableList(list);
    }

    private ObservableList<Protocol> getConsumerProtocolList() {
        ArrayList<Protocol> list = new ArrayList<Protocol>();
        list.add(Protocol.ACTIVE_MQ);
        list.add(Protocol.RABBIT_MQ);
        list.add(Protocol.STREAM_SERVICE);
        list.add(Protocol.TCP);
        list.add(Protocol.TCP_SERVER);
        list.add(Protocol.WEBSOCKETS);
        list.add(Protocol.KAFKA);
        return FXCollections.observableList(list);
    }

    /**
     * FIXME: This is a hardcoding method - we need to get rid oof this and
     * introspec the Producer or Consumer for its individual properties
     */
    private void initPropertiesCache() {
        // Producers
        producerPropertiesCache = new HashMap<Protocol, ObservableList<Property>>();
        Property simulationFile = new Property("simulationFile", "");

        // TCP
        ArrayList<Property> props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("hosts", "localhost"));
        props.add(new Property("port", "5565"));
        producerPropertiesCache.put(Protocol.TCP, FXCollections.observableList(props));

        // TCP SERVER
        props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("hosts", "localhost"));
        props.add(new Property("port", "5565"));
        producerPropertiesCache.put(Protocol.TCP_SERVER, FXCollections.observableList(props));

        // ACTIVE_MQ
        props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("providerUrl", ""));
        props.add(new Property("destinationType", ""));
        props.add(new Property("destinationName", ""));
        producerPropertiesCache.put(Protocol.ACTIVE_MQ, FXCollections.observableList(props));

        // KAFKA
        props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("brokerList", "localhost:9092"));
        props.add(new Property("topic", "default-topic"));
        props.add(new Property("requiredAcks", "1"));
        producerPropertiesCache.put(Protocol.KAFKA, FXCollections.observableList(props));

        // RABBIT_MQ
        props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("uri", ""));
        props.add(new Property("exchangeName", ""));
        props.add(new Property("queueName", ""));
        props.add(new Property("routingKey", ""));
        producerPropertiesCache.put(Protocol.RABBIT_MQ, FXCollections.observableList(props));

        // STREAM_SERVICE
        props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("hosts", "localhost"));
        props.add(new Property("port", "6180"));
        props.add(new Property("serviceName", "vehicles"));
        props.add(new Property("connectionCount", "1"));
        producerPropertiesCache.put(Protocol.STREAM_SERVICE, FXCollections.observableList(props));

        // WEBSOCKETS
        props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("url", ""));
        props.add(new Property("connectionCount", "1"));
        producerPropertiesCache.put(Protocol.WEBSOCKETS, FXCollections.observableList(props));

        // WEBSOCKETS_SERVER
        props = new ArrayList<Property>();
        props.add(simulationFile);
        props.add(new Property("port", "5565"));
        producerPropertiesCache.put(Protocol.WEBSOCKET_SERVER, FXCollections.observableList(props));

        // Producers
        consumerPropertiesCache = new HashMap<Protocol, ObservableList<Property>>();

        // TCP
        props = new ArrayList<Property>();
        props.add(new Property("hosts", "localhost"));
        props.add(new Property("port", "5565"));
        consumerPropertiesCache.put(Protocol.TCP, FXCollections.observableList(props));

        //TCP_SERVER
        props = new ArrayList<Property>();
        props.add(new Property("port", "5775"));
        consumerPropertiesCache.put(Protocol.TCP_SERVER, FXCollections.observableList(props));

        // ACTIVE_MQ
        props = new ArrayList<Property>();
        props.add(new Property("providerUrl", ""));
        props.add(new Property("destinationType", ""));
        props.add(new Property("destinationName", ""));
        consumerPropertiesCache.put(Protocol.ACTIVE_MQ, FXCollections.observableList(props));

        // RABBIT_MQ
        props = new ArrayList<Property>();
        props.add(new Property("uri", ""));
        props.add(new Property("exchangeName", ""));
        props.add(new Property("queueName", ""));
        props.add(new Property("routingKey", ""));
        consumerPropertiesCache.put(Protocol.RABBIT_MQ, FXCollections.observableList(props));

        // STREAM_SERVICE
        props = new ArrayList<Property>();
        props.add(new Property("hosts", "localhost"));
        props.add(new Property("port", "6180"));
        props.add(new Property("serviceName", "vehicles"));
        props.add(new Property("connectionCount", "1"));
        consumerPropertiesCache.put(Protocol.STREAM_SERVICE, FXCollections.observableList(props));

        // WEBSOCKETS
        props = new ArrayList<Property>();
        props.add(new Property("url", ""));
        props.add(new Property("connectionCount", "1"));
        consumerPropertiesCache.put(Protocol.WEBSOCKETS, FXCollections.observableList(props));

        // KAFKA
        props = new ArrayList<Property>();
        props.add(new Property("zookeeper", "localhost:2181"));
        props.add(new Property("consumergroup", "consumergroup1"));
        props.add(new Property("topic", "testtopic"));
        props.add(new Property("numthreads", "1"));
        consumerPropertiesCache.put(Protocol.KAFKA, FXCollections.observableList(props));
    }

    /**
     * Helper static class to create delete button
     *
     * @param <T>
     */
    private class DeleteableTableCell<T> extends TableCell<ConnectableRemoteHost, Boolean> {

        //statics

        private static final String DELETE_IMAGE_SOURCE = "images/delete.png";

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            setAlignment(Pos.CENTER);

            if (empty) {
                setGraphic(null);
                return;
            }

            final ConnectableRemoteHost remoteHost = (ConnectableRemoteHost) getTableRow().getItem();
            final TableView<ConnectableRemoteHost> table = getTableView();
            Button deleteButton = new Button("", new ImageView(new Image(FixtureController.class.getResourceAsStream(DELETE_IMAGE_SOURCE))));
            deleteButton.getStyleClass().add("buttons");
            deleteButton.setTooltip(new Tooltip(UIMessages.getMessage("UI_DELETE_DESC")));
            deleteButton.setOnAction(event -> {
                table.getItems().remove(remoteHost);
                if (table.getId().contains("producers")) {
                    deleteProducer(remoteHost);
                } else {
                    deleteConsumer(remoteHost);
                }
            });
            setGraphic(deleteButton);
        }
    }

    /**
     * Helper static class to create connected/disconnected buttons based on the
     * a boolean property
     *
     * @param <T>
     */
//  private class ConnectedTableCell<T> extends TableCell<ConnectableRemoteHost, Boolean> 
//  {	
//  	//statics
//  	private static final String CONNECTED_IMAGE_SOURCE = "images/connected.png"; 
//  	private static final String DISCONNECTED_IMAGE_SOURCE = "images/disconnected.png"; 
//    
//  	@Override
//  	protected void updateItem(Boolean item, boolean empty)
//  	{
//  		super.updateItem(item, empty);
//  		setAlignment(Pos.CENTER);
//  		
//  		if( empty || item == null )
//  		{
//  			setGraphic(null);
//  			return;
//  		}
//
//  		ConnectableRemoteHost remoteHost = (ConnectableRemoteHost) getTableRow().getItem();
//  		if(item)
//  		{
//  			setGraphic(getConnectedButton(remoteHost));
//  		}
//  		else
//  		{
//  			setGraphic(getDisconnectedButton(remoteHost));
//  		}
//  	}
//  	
//  	private Button getConnectedButton(final ConnectableRemoteHost remoteHost)
//  	{
//  		Button connectedButton = new Button("", new ImageView(new Image(FixtureController.class.getResourceAsStream(CONNECTED_IMAGE_SOURCE))));
//    	connectedButton.getStyleClass().add("buttons");
//    	connectedButton.setTooltip(new Tooltip(UIMessages.getMessage("UI_DISCONNECT_DESC")));
//    	connectedButton.setOnAction(event -> {
//    		connect(remoteHost);
//    	});
//    	return connectedButton;
//  	}
//  	
//  	private Button getDisconnectedButton(final ConnectableRemoteHost remoteHost)
//  	{
//  		Button disconnectedButton = new Button("", new ImageView(new Image(FixtureController.class.getResourceAsStream(DISCONNECTED_IMAGE_SOURCE))));
//  		disconnectedButton.getStyleClass().add("buttons");
//    	disconnectedButton.setTooltip(new Tooltip(UIMessages.getMessage("UI_CONNECT_DESC")));
//    	disconnectedButton.setOnAction(event -> {
//    		disconnect(remoteHost);
//    	});
//    	return disconnectedButton;
//  	}
//  }
}
