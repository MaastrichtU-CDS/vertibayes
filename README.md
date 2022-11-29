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

### Response example:

```
{
  "nodes" : [ {
    "parents" : [ ],
    "name" : "x1",
    "type" : "real",
    "probabilities" : [ {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "0.5",
        "lowerLimit" : "-inf",
        "range" : true
      },
      "parentValues" : [ ],
      "p" : 0.4010119726339795
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "inf",
        "lowerLimit" : "0.5",
        "range" : true
      },
      "parentValues" : [ ],
      "p" : 0.5989880273660205
    } ],
    "bins" : [ {
      "upperLimit" : "inf",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-inf"
    } ]
  }, {
    "parents" : [ "x1" ],
    "name" : "x2",
    "type" : "real",
    "probabilities" : [ {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "0.5",
        "lowerLimit" : "-inf",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.9987562189054726
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "inf",
        "lowerLimit" : "0.5",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.0012437810945273632
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "0.5",
        "lowerLimit" : "-inf",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.5323578396383536
    }, {
      "localValue" : {
        "localValue" : null,
        "upperLimit" : "inf",
        "lowerLimit" : "0.5",
        "range" : true
      },
      "parentValues" : [ {
        "parent" : "x1",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.46764216036164646
    } ],
    "bins" : [ {
      "upperLimit" : "inf",
      "lowerLimit" : "0.5"
    }, {
      "upperLimit" : "0.5",
      "lowerLimit" : "-inf"
    } ]
  }, {
    "parents" : [ "x2" ],
    "name" : "x3",
    "type" : null,
    "probabilities" : [ {
      "localValue" : {
        "localValue" : "0",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.3986727416798732
    }, {
      "localValue" : {
        "localValue" : "1",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "0.5",
          "lowerLimit" : "-inf",
          "range" : true
        }
      } ],
      "p" : 0.6013272583201268
    }, {
      "localValue" : {
        "localValue" : "0",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.002793296089385475
    }, {
      "localValue" : {
        "localValue" : "1",
        "upperLimit" : null,
        "lowerLimit" : null,
        "range" : false
      },
      "parentValues" : [ {
        "parent" : "x2",
        "value" : {
          "localValue" : null,
          "upperLimit" : "inf",
          "lowerLimit" : "0.5",
          "range" : true
        }
      } ],
      "p" : 0.9972067039106145
    } ],
    "bins" : [ ]
  } ],
  "syntheticTrainingAuc": 0.49951998702044903
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
