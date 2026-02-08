# Contributing to Digital Dark

Thank you for your interest in contributing!

## How to Contribute

1. **Fork** this repository
2. **Create a branch** for your feature or fix: `git checkout -b my-feature`
3. **Make your changes** and test them
4. **Commit** with a clear message: `git commit -m "Add: description of change"`
5. **Push** your branch and open a **Pull Request**

## Building from Source

### Requirements
- Java 8 (JDK)
- Maven 3.6+

### Build
```bash
mvn clean install -Pno-git-rev -Dcheckstyle.skip=true -DskipTests
```

The output JAR will be in `target/`.

## Code Style

- Follow the existing code conventions in the project
- Dark mode should remain the default — do not add a light mode toggle
- New UI elements must be dark-theme-aware (use `ColorScheme.getColor()`)
- Test your changes visually before submitting

## Reporting Issues

- Use GitHub Issues
- Include a screenshot if it's a visual bug
- Describe the steps to reproduce the problem

## License

By contributing, you agree that your contributions will be licensed under the **GPL v3** license (see [LICENSE](LICENSE)).

## Acknowledgments

Digital Dark builds upon [hneemann/Digital](https://github.com/hneemann/Digital) by Helmut Neemann.
