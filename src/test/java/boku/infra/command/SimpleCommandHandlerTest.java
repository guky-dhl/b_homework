package boku.infra.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleCommandHandlerTest {
    final TestCommand command = new TestCommand("Result");

    @Test
    void should_handle_command() {
        final var subject = new SimpleCommandHandler();
        subject.add(new TestCommandHandler());


        final var result = subject.handle(command);

        assertEquals(command.result, result);
    }

    @Test
    void should_throw_missing_command_when_no_handler_provided() {
        final var subject = new SimpleCommandHandler();

        final var exception = assertThrows(MissingHandler.class, () -> subject.handle(command));
        assertTrue(exception.getMessage().contains(TestCommand.class.toString()));
    }

    static class TestCommand implements Command<String> {
        final String result;

        TestCommand(String result) {
            this.result = result;
        }
    }

    static class TestCommandHandler implements Command.Handler<TestCommand, String> {

        @Override
        public String handle(TestCommand command) {
            return command.result;
        }

        @Override
        public Class<TestCommand> commandClass() {
            return TestCommand.class;
        }
    }
}