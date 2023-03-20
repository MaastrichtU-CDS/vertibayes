# VertiBayes

This project implements various bayesian network prototols using the n-party scalar project protocol
library ( https://github.com/MaastrichtU-CDS/n-scalar-product-protocol )

## Preprint:

More information about the algorithm used can be found here:
https://arxiv.org/abs/2210.17228

#### Privacy

This implementation relies on the n-party scalar product protocol mentioned earlier, and as such relies on a trusted
third party, a different scalar product protocol could work without a trusted third party.

The output of this implementation is a bayesian network. It is important to note that a bayesian network reveals P(
X=xi|Y=yi) for each of it's attributes. Combined with knowledge about the population size this means that publishing a
bayesian network can reveal the counts of certain attribute values, as well as the counts of certain combinations. This
information can potentially be used to reconstruct the original database. Because of this it is important to make sure
when publishing the bayesian network that the population size is not released to untrusted parties.

It is important to note that repeat attempts to build a network with different predefined bins & different predefined
network structures can be combined to reveal more information than a single attempt, making it easier to rebuild the
original data. To protect against this good governance should be used. For example, only allow automatically generated
structures & bins, ensuring each attempt results in the same network. Or by ensuring that only the best performing
bayesian network is published, while the other attempts are kept secret.

## Project setup:

This project uses Spring boot at its basis. The following properties are needed:

```
servers=<list of urls for the other servers>
secretServer=<url to the commodity server>
datapath=<path to a csv containing local data>
server.port=<port to be used>
server=<server id>
```

## Data setup:

The data can be presented in three formats:

1) A weka .arff
2) A .parquet file
3) A .csv file

The vantage6 wrapper assumes a .csv file. Work is currently being done to allow more flexibility within the wrapper.
https://github.com/vantage6/vantage6/issues/398

When using a csv it needs to use the following format. The top row is assumed to contain the typing of the attributes (
bool, string, numeric, real). The second row is assumed to be attribute ID's. We also assume the first collumn to
contain ID's. The assumption is that the first collumn contains the recordId's.

### Unknown data

It is assumed unknown data has the value of '?'

### Other assumptions:

It is assumed values do not contain spaces, similarly it is assumed that the various keywords in a WEKA bif file are not
used, as well as those used in a WEKA arff file.

Lastly "All" is a reserved keyword for a bin that contains all possible values for a given attribute.

## Implemented methods:

K2: determines network structure Maximum likelhood: Assigns probabilities to the values in nodes based on parent-child
counts (e.g. calculates P(A|B))
Expectation Maximization 2-step approach:

- Step one: Train a network using maximum likelyhood. Then use this initial network to generate synthetic data
- Step two: Train a second network on this synthetic data using EM implemented in WEKA

Both the K2 and Maximum Likelyhood protocols rely on the n-party scalar product protocol

Continuous data can be binned

During structure learning missing data will be replaced by mean mode values depending on the type of attribute.

There are three ways to call the algorithm;

1) With a predefined structure
2) With a predefined structure that needs to be expanded by K2. This can also be used for attribute selection
3) Without a predefined structure

### Input data:

#### Handling a Hybird split

To handle a Hybrid split in your data include an attributecolumn in all relevant datasets named "locallyPresent" with "
bool" as it's type. Locally available data should have the value "TRUE". Missing records are then inserted as a row that
has the value "FALSE" for this attribute. This should be handled in a preprocessing step.

Important to note; datasets still need to have the same ordering for their records. It is assumed that recordlinkage is
handled in a preprocessing step as well.

This functionality is only available in the java implementation.

### Request Example createNetwork

It is possible to indicate the minPercentage for binning, or to manually define bins for use during K2.

```
{
  "minPercentage" : 10,
  "nodes" : [ {
    "parents" : [ ],
    "name" : "x1",
    "type" : "numeric",
    "probabilities" : [ ],
    "bins" : [ {
      "upperLimit" : "1.5",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-0.5"
    } ]
  }, {
    "parents" : [ ],
    "name" : "x2",
    "type" : "numeric",
    "probabilities" : [ ],
    "bins" : [ {
      "upperLimit" : "1.5",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-0.5"
    } ]
  }, {
    "parents" : [ ],
    "name" : "x3",
    "type" : "string",
    "probabilities" : [ ],
    "bins" : [ ]
  } ]
}
```

### Request example Expectation Maximization

The request for Maximum Likelyhood is similar except it does not contain a target.

```
{
  "nodes" : [ {
    "parents" : [ ],
    "name" : "x1",
    "type" : "numeric",
    "probabilities" : [ ],
    "bins" : [ {
      "upperLimit" : "1.5",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-1"
    }, {
      "upperLimit" : "?",
      "lowerLimit" : "?"
    } ],
    "discrete" : true
  }, {
    "parents" : [ "x1" ],
    "name" : "x2",
    "type" : "real",
    "probabilities" : [ ],
    "bins" : [ {
      "upperLimit" : "1.5",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-1"
    }, {
      "upperLimit" : "?",
      "lowerLimit" : "?"
    } ],
    "discrete" : false
  }, {
    "parents" : [ "x2","x1" ],
    "name" : "x3",
    "type" : "string",
    "probabilities" : [ ],
    "bins" : [ ],
    "discrete" : true
  } ],
  "target" : "x3"
  "minPercentage":"0.1",
  "folds":1,
  "trainStructure" : true
}
```

Important to note:
Bins can be set manually when expert knowledge is available. They can also be determined automatically at which point a
maximum of 10 bins will be made, and a minimum of 1 bin. Each bin will attempt to pick the smallest unique values that
contain at least 10 individuals and 10% of the population. This is the default setting. It is also possible to create
bins a minimum of 20%, 25%, 30% or 40% of the population. Other settings are not possible. If the current bin cannot be
made large enough to achieve this it will be merged with the last bin.

They will be automatically generated if the attribute in question is a number (real or integer) and the bins were left
empty in the request.

The trainstructure flag is used to trigger K2.

### Response example:

```
{
  "nodes" : [ {
    "parents" : [ ],
    "name" : "asia",
    "type" : "string",
    "probabilities" : [ {
      "localValue" : {
        "localValue" : "no",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ ],
      "p" : 0.9874512548745126
    }, {
      "localValue" : {
        "localValue" : "yes",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ ],
      "p" : 0.012548745125487452
    } ],
    "bins" : [ ]
  }, {
    "parents" : [ "asia" ],
    "name" : "either",
    "type" : "string",
    "probabilities" : [ {
      "localValue" : {
        "localValue" : "no",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "asia",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.9384872417982989
    }, {
      "localValue" : {
        "localValue" : "yes",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "asia",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.061512758201701094
    }, {
      "localValue" : {
        "localValue" : "no",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "asia",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.8293650793650794
    }, {
      "localValue" : {
        "localValue" : "yes",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "asia",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.17063492063492064
    } ],
    "bins" : [ ]
  }, {
    "parents" : [ "either", "asia" ],
    "name" : "lung",
    "type" : "string",
    "probabilities" : [ {
      "localValue" : {
        "localValue" : "no",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.9986514187075197
    }, {
      "localValue" : {
        "localValue" : "yes",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.0013485812924803108
    }, {
      "localValue" : {
        "localValue" : "no",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.9952380952380953
    }, {
      "localValue" : {
        "localValue" : "yes",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.004761904761904762
    }, {
      "localValue" : {
        "localValue" : "no",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.11759868421052631
    }, {
      "localValue" : {
        "localValue" : "yes",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "no",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.8824013157894737
    }, {
      "localValue" : {
        "localValue" : "no",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.5227272727272727
    }, {
      "localValue" : {
        "localValue" : "yes",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "either",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      }, {
        "parent" : "asia",
        "value" : {
          "localValue" : "yes",
          "upperLimit" : null,
          "lowerLimit" : null,
          "range" : false
        }
      } ],
      "p" : 0.4772727272727273
    } ],
    "bins" : [ ]
  } ],
  "scvAuc" : 0.5270323037974682,
  "svdgAuc" : 0.5596742377548273,
  "openMarkov" : "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<ProbModelXML formatVersion=\"0.2.0\">\r\n  <ProbNet type=\"BayesianNetwork\">\r\n    <DecisionCriteria>\r\n      <Criterion name=\"---\" unit=\"---\"/>\r\n    </DecisionCriteria>\r\n    <Properties/>\r\n    <Variables>\r\n      <Variable name=\"asia\" role=\"chance\" type=\"finiteStates\">\r\n        <States>\r\n          <State name=\"no\"/>\r\n          <State name=\"yes\"/>\r\n        </States>\r\n      </Variable>\r\n      <Variable name=\"either\" role=\"chance\" type=\"finiteStates\">\r\n        <States>\r\n          <State name=\"no\"/>\r\n          <State name=\"yes\"/>\r\n        </States>\r\n      </Variable>\r\n      <Variable name=\"lung\" role=\"chance\" type=\"finiteStates\">\r\n        <States>\r\n          <State name=\"no\"/>\r\n          <State name=\"yes\"/>\r\n        </States>\r\n      </Variable>\r\n    </Variables>\r\n    <Links>\r\n      <Link directed=\"true\">\r\n        <Variable name=\"asia\"/>\r\n        <Variable name=\"either\"/>\r\n      </Link>\r\n      <Link directed=\"true\">\r\n        <Variable name=\"either\"/>\r\n        <Variable name=\"lung\"/>\r\n      </Link>\r\n      <Link directed=\"true\">\r\n        <Variable name=\"asia\"/>\r\n        <Variable name=\"lung\"/>\r\n      </Link>\r\n    </Links>\r\n    <Potentials>\r\n      <Potential role=\"conditionalProbability\" type=\"Table\">\r\n        <Variables>\r\n          <Variable name=\"asia\"/>\r\n        </Variables>\r\n        <Values>0.9874512548745126 0.012548745125487452</Values>\r\n      </Potential>\r\n      <Potential role=\"conditionalProbability\" type=\"Table\">\r\n        <Variables>\r\n          <Variable name=\"either\"/>\r\n          <Variable name=\"asia\"/>\r\n        </Variables>\r\n        <Values>0.9384872417982989 0.061512758201701094 0.8293650793650794 0.17063492063492064</Values>\r\n      </Potential>\r\n      <Potential role=\"conditionalProbability\" type=\"Table\">\r\n        <Variables>\r\n          <Variable name=\"lung\"/>\r\n          <Variable name=\"either\"/>\r\n          <Variable name=\"asia\"/>\r\n        </Variables>\r\n        <Values>0.9986514187075197 0.0013485812924803108 0.9952380952380953 0.004761904761904762 0.11759868421052631 0.8824013157894737 0.5227272727272727 0.4772727272727273</Values>\r\n      </Potential>\r\n    </Potentials>\r\n  </ProbNet>\r\n  <InferenceOptions>\r\n    <MulticriteriaOptions>\r\n      <SelectedAnalysisType>UNICRITERION</SelectedAnalysisType>\r\n      <Unicriterion>\r\n        <Scales>\r\n          <Scale Criterion=\"---\" Value=\"1.0\"/>\r\n        </Scales>\r\n      </Unicriterion>\r\n      <CostEffectiveness>\r\n        <Scales>\r\n          <Scale Criterion=\"---\" Value=\"1.0\"/>\r\n        </Scales>\r\n        <CE_Criteria>\r\n          <CE_Criterion Criterion=\"---\" Value=\"Cost\"/>\r\n        </CE_Criteria>\r\n      </CostEffectiveness>\r\n    </MulticriteriaOptions>\r\n  </InferenceOptions>\r\n</ProbModelXML>\r\n"
}
```

### Additional input/output:

If the field ```"openMarkovResponse":"true"``` is included in the input JSON the output will contain a
field ```openMarkov``` which contains the network in BIF format as used by the openMarkov library.

### Crossfold validation:

There are 3 ways to validate the model as detailed in https://arxiv.org/abs/2210.17228

1) validation against a public testset.
2) Using SCV validation (see section 2.4)
3) Using SVDG validation (see section 2.4)

To use SVDG the field "folds" needs to be set in the request. This indicates the number of crossfolds that will be used.
1 fold means there is no crossfold validation, and SVDG cannot be used. The maximum is 10 folds.
