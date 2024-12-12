1. Знайти мінімальний елемент у двовимірному великому масиві,
значення якого вдвічі більше першого згенерованого елементу.
Кількість елементів масиву, має задавати користувач. Значення
елементів генеруйте рандомно.
У результаті на екран має бути виведено згенерований масив,
результат виконання задачі та час роботи програми.

Обираємо массив 100 на 100.
Work stealing виконується у середньому за 1-2ms.
Work dealing виконується у середньому за 5-6ms.
Так відбувається, тому що підход work-stealing дозволяє використати максимальні можливості кожного потока для обчислення необхідної інформації. У варіанті work dealing потоки можуть бути вільними(після виконання задач) і чекати на інший поток, який ще не завершив виконання. 


2. Напишіть програму, яка буде проходити по файлам певної
директорії та знаходити усі файли текстового формату та читати їх
вміст, підраховуючи кількість символів.
Директорію має обирати користувач.
У результаті потрібно виводити ім’я файлу та кількість символів у
ньому.

Використовуємо директорію з 4ма файлами .txt. Довжина тексту в кожному від 10 до 30 літер.
Work stealing виконується у середньому за 11-12ms.
Work dealing виконується у середньому за 5-6ms.

