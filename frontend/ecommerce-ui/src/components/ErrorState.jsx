function ErrorState({ message }) {
  if (!message) {
    return null;
  }

  return <div className="state-box error-box">{message}</div>;
}

export default ErrorState;
