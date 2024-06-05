
package com.to.kafka.examples.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Campaigner",
    "AdImpressions",
    "AdClicks",
    "AddCTR"
})
public class CampaignPerformance {

    @JsonProperty("Campaigner")
    private String campaigner;
    @JsonProperty("AdImpressions")
    private Long adImpressions;
    @JsonProperty("AdClicks")
    private Long adClicks;
    @JsonProperty("AddCTR")
    private Double addCTR;

    @JsonProperty("Campaigner")
    public String getCampaigner() {
        return campaigner;
    }

    @JsonProperty("Campaigner")
    public void setCampaigner(String campaigner) {
        this.campaigner = campaigner;
    }

    public CampaignPerformance withCampaigner(String campaigner) {
        this.campaigner = campaigner;
        return this;
    }

    @JsonProperty("AdImpressions")
    public Long getAdImpressions() {
        return adImpressions;
    }

    @JsonProperty("AdImpressions")
    public void setAdImpressions(Long adImpressions) {
        this.adImpressions = adImpressions;
    }

    public CampaignPerformance withAdImpressions(Long adImpressions) {
        this.adImpressions = adImpressions;
        return this;
    }

    @JsonProperty("AdClicks")
    public Long getAdClicks() {
        return adClicks;
    }

    @JsonProperty("AdClicks")
    public void setAdClicks(Long adClicks) {
        this.adClicks = adClicks;
    }

    public CampaignPerformance withAdClicks(Long adClicks) {
        this.adClicks = adClicks;
        return this;
    }

    @JsonProperty("AddCTR")
    public Double getAddCTR() {
        return addCTR;
    }

    @JsonProperty("AddCTR")
    public void setAddCTR(Double addCTR) {
        this.addCTR = addCTR;
    }

    public CampaignPerformance withAddCTR(Double addCTR) {
        this.addCTR = addCTR;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("campaigner", campaigner).append("adImpressions", adImpressions).append("adClicks", adClicks).append("addCTR", addCTR).toString();
    }

}
