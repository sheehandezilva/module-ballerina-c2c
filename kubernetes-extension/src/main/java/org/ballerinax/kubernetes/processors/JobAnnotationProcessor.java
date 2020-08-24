/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.JobModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_CERT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_HOST;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.convertRecordFields;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;

/**
 * Job Annotation processor.
 */
public class JobAnnotationProcessor extends AbstractAnnotationProcessor {

    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        JobModel jobModel = new JobModel();
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
                convertRecordFields(((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr)
                        .getFields());
        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : keyValues) {
            JobConfiguration jobConfiguration =
                    JobConfiguration.valueOf(keyValue.getKey().toString());
            if (jobConfiguration == JobConfiguration.schedule) {
                String minutes = null, hours = null, dayOfMonth = null, monthOfYear = null, daysOfWeek = null;
                for (RecordLiteralNode.RecordField recordField :
                        ((BLangRecordLiteral) keyValue.getValue()).getFields()) {
                    BLangRecordLiteral.BLangRecordKeyValueField schedule =
                            (BLangRecordLiteral.BLangRecordKeyValueField) recordField;
                    ScheduleConfig scheduleConfig = ScheduleConfig.valueOf(schedule.getKey().toString());
                    switch (scheduleConfig) {
                        case minutes:
                            minutes = getStringValue(schedule.getValue());
                            break;
                        case hours:
                            hours = getStringValue(schedule.getValue());
                            break;
                        case dayOfMonth:
                            dayOfMonth = getStringValue(schedule.getValue());
                            break;
                        case monthOfYear:
                            monthOfYear = getStringValue(schedule.getValue());
                            break;
                        case daysOfWeek:
                            daysOfWeek = getStringValue(schedule.getValue());
                            break;
                        default:
                            break;
                    }
                }
                jobModel.setSchedule(minutes + " " + hours + " " + dayOfMonth + " "
                        + monthOfYear + " " + daysOfWeek);
            }
        }
        String dockerHost = System.getenv(DOCKER_HOST);
        if (!isBlank(dockerHost)) {
            jobModel.setDockerHost(dockerHost);
        }
        String dockerCertPath = System.getenv(DOCKER_CERT_PATH);
        if (!isBlank(dockerCertPath)) {
            jobModel.setDockerCertPath(dockerCertPath);
        }
        KubernetesContext.getInstance().getDataHolder().setJobModel(jobModel);
    }


    /**
     * Enum class for JobConfiguration.
     */
    private enum JobConfiguration {
        schedule,
    }

    private enum ScheduleConfig {
        minutes,
        hours,
        dayOfMonth,
        monthOfYear,
        daysOfWeek
    }
}
