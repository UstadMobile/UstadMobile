    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()+3).padStart(2, '0');
    const time = `${hours}:${minutes}`;
    output.result1 = time;