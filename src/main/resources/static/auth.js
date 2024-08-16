        async function login(event) {
            event.preventDefault(); // Предотвращаем отправку формы

            const username = document.querySelector('input[name="username"]').value;
            const password = document.querySelector('input[name="password"]').value;

            const response = await fetch('/api/v1/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();
                // Сохраняем refresh token в локальном хранилище
                localStorage.setItem('refreshToken', data.refreshToken);
                // Перенаправляем пользователя на защищенную страницу
                window.location.href = '/api/reservations';
            } else {
                alert('Неверный логин или пароль');
            }
        }

async function fetchWithAuth(url, options = {}) {
    let response = await fetch(url, {
        ...options,
        credentials: 'include' // Обеспечивает отправку куки с запросом
    });

    if (response.status === 401) {
        // Попытка обновления access token
        const refreshSuccess = await refreshAccessToken();
        if (refreshSuccess) {
            // Повторяем оригинальный запрос после обновления токена
            response = await fetch(url, {
                ...options,
                credentials: 'include'
            });
        } else {
            alert('Не удалось обновить токен, пожалуйста, войдите снова.');
            // Перенаправляем пользователя на страницу входа
            window.location.href = '/login';
        }
    }

    return response;
}

async function refreshAccessToken() {
    const refreshToken = localStorage.getItem('refreshToken');

    const response = await fetch('/api/v1/auth/refresh', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ token: refreshToken })
    });

    if (response.ok) {
        const data = await response.json();
        // Обновляем access token в куках
        document.cookie = `accessToken=${data.accessToken}; path=/; secure; HttpOnly`;
        return true;
    } else {
        alert('Не удалось обновить access token');
        // Перенаправляем пользователя на страницу входа
        window.location.href = '/login';
        return false;
    }
}
async function validateUsername() {
            const username = document.getElementById("username").value;
            const userId = document.getElementById("userId").value;
            const response = await fetch(`/api/users/checkUsername?username=${username}&userId=${userId}`);
            const result = await response.json();
            return result.unique;
        }

async function validateForm(event) {
      event.preventDefault();  // Останавливаем отправку формы

      const loginInput = document.getElementById("username");
      const loginPattern = /^[a-zA-Z0-9]+$/;
      let valid = true;

      // Проверка формата логина
      if (!loginPattern.test(loginInput.value)) {
          loginInput.classList.add("is-invalid");
          valid = false;
      } else {
          loginInput.classList.remove("is-invalid");
      }

      // Проверка уникальности логина
      const usernameUnique = await validateUsername();
      if (!usernameUnique) {
           document.getElementById("username-unique-feedback").style.display = 'block';
           valid = false;
      } else {
           document.getElementById("username-unique-feedback").style.display = 'none';
      }

      // Если форма валидна, отправляем её
      if (valid) {
           document.getElementById("registrationForm").submit();
      }
           return false;
      }

        function validateTime(event) {
            event.preventDefault();  // Останавливаем отправку формы

            const dateTimeInput = document.getElementById("startDateTime");
            const selectedDateTime = new Date(dateTimeInput.value);
            const currentDateTime = new Date();
            currentDateTime.setHours(currentDateTime.getHours() + 1);

            let valid = true;

            // Проверка, что выбранное время не раньше текущего времени + 1 час
            if (selectedDateTime < currentDateTime) {
                dateTimeInput.classList.add("is-invalid");
                valid = false;
            } else {
                dateTimeInput.classList.remove("is-invalid");
            }

            // Если время валидно, отправляем форму
            if (valid) {
                document.getElementById("reservationForm").submit();
            }

            return false;
        }

async function validateFormPasswordRecovery(event) {
    event.preventDefault();  // Останавливаем отправку формы

    console.log("Форма восстановления пароля: валидация начата");

    const loginInput = document.getElementById("username");
    const loginPattern = /^[a-zA-Z0-9]+$/;
    let valid = true;

    // Проверка формата логина
    if (!loginPattern.test(loginInput.value)) {
        console.log("Логин не соответствует требованиям");
        loginInput.classList.add("is-invalid");
        valid = false;
    } else {
        console.log("Логин соответствует требованиям");
        loginInput.classList.remove("is-invalid");
    }

      const usernameUnique = await validateUsername();
      if (usernameUnique) {
           document.getElementById("username-unique-feedback").style.display = 'block';
           valid = false;
      } else {
           document.getElementById("username-unique-feedback").style.display = 'none';
      }

    // Если форма валидна, отправляем её
    if (valid) {
        console.log("Форма валидна, отправка...");
        document.getElementById("passwordRecoveryForm").submit();
    } else {
        console.log("Форма невалидна, отправка отменена");
    }

    return false;
}