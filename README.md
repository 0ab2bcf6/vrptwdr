# VRPTWDR Solver Framework

[![License](https://img.shields.io/badge/license-BSD%202--Clause-blue)](./LICENSE)
[![Java Version](https://img.shields.io/badge/java-11%2B-orange)](https://www.java.com/)

This repository provides a framework for solving the Vehicle Routing Problem with Time Windows and Delivery Robots (VRPTWDR). The codebase includes three solvers:

1. **Greedy Solver** – A simple heuristic-based approach.
2. **Adaptive Large Neighborhood Search with Simulated Annealing (ALNS-SA) Solver** – A metaheuristic optimization method.
3. **Gurobi Solver** – A solver using the Gurobi optimizer for improved solution quality.

## Status
This repository is **not actively developed** but provides a functional framework for working with VRPTWDR. Contributions and modifications are welcome.

## Requirements
- **Java 11+** is required to run the framework.
- **Gurobi** must be installed for the Gurobi Solver to function properly.

## TODOs
- Test the newly implemented Solomon and Homberger parsers. `src/parser/SolomonHombergerXMLParser.java` is very inefficient.
- Ensure the correct calculation of VRPTW cost for Solomon and Homberger benchmark instances.

## Usage
The framework is designed for researchers and practitioners looking to experiment with VRPTWDR solutions. Users can integrate new solvers and modify the existing solvers as needed.

- Instances and Solvers should be selected in `src/Main.java`.
- Solvers should be represented as a class in `algorithm/MyCustomerSolver.java`
- Custom parser should implement `src/parser/InterfaceVRPTWDRDataset.java`
- Custom operators for the ALNS metaheuristic solver should extend/implement the respective abstract class and interface
    - `src/insert/AbstractALNSInsert.java` and `src/insert/InterfaceALNSInsert.java` for inserts
    - `src/removal/AbstractALNSRemoval.java` and `src/removal/InterfaceALNSRemoval.java` for removal

## License
This project is licensed under the BSD 2-Clause License. See the `LICENSE` file for details.

