function addLabel() {
    const input = document.getElementById('new-label');
    const value = input.value.trim();
    if (!value) return;

    const li = document.createElement('li');

    const span = document.createElement('span');
    span.textContent = value;
    li.appendChild(span);

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'my-button-outline';
    btn.textContent = 'Remove';
    btn.onclick = function() { removeLabel(this); };
    li.appendChild(btn);

    const hidden = document.createElement('input');
    hidden.type = 'hidden';
    hidden.name = 'label';
    hidden.value = value;
    li.appendChild(hidden);

    document.getElementById('label-edit-list').appendChild(li);
    input.value = '';
    input.focus();
}

function removeLabel(btn) {
    btn.closest('li').remove();
}
