# VertiBayes

This project implements various bayesian network prototols using the n-party scalar project protocol
library ( https://gitlab.com/fvandaalen/n-scalar-product-protocol )

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

The project currently expects data to be presented in a csv. The top row is assumed to contain the typing of the
attributes (bool, string, number). The second row is assumed to be attribute ID's. We also assume the first collumn to
contain ID's. The assumption is that the first collumn contains the recordId's.

### Unknown data

It is assumed unknown data has the value of '?'

## Implemented methods:

K2: determines network structure Maximum likelhood: Assigns probabilities to the values in nodes based on parent-child
counts (e.g. calculates P(A|B))
Currently the maximum likelyhood treats all attributes as discrete.

Both of these rely on the n-party protocol

### Util methods:

There is a testclass that can be used to generate synthetic data based on vertically split data for the purposes of easy
experiments.


