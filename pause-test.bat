@echo on
echo Тест команды pause

echo Тест 1: стандартный pause
pause

echo Тест 2: pause с перенаправлением
pause > nul

echo Тест 3: choice
choice /C Y /N /M "Нажмите Y для продолжения"

echo Тест 4: echo и set /p
echo Нажмите любую клавишу для продолжения...
set /p temp=""

echo Тест 5: timeout
timeout /t 5

echo Тест завершен 