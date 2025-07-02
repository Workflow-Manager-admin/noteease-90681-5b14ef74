#!/bin/bash
cd /home/kavia/workspace/code-generation/noteease-90681-5b14ef74/frontend_notes_app
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

